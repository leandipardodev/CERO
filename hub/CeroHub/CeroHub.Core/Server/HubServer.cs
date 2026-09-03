using System.Net;
using System.Net.Security;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;

namespace CeroHub.Core.Server;

/// <summary>
/// Servidor WebSocket que recibe datos del celular (CERO app) y los
/// reenvía a los manejadores de utilidades. Usa TcpListener directamente
/// (sin http.sys) para no requerir privilegios de administrador ni URL ACL.
/// </summary>
public sealed class HubServer : IAsyncDisposable
{
    private readonly int _port;
    private readonly IMessageHandler _handler;
    private TcpListener? _listener;
    private CancellationTokenSource _cts = new();
    private readonly List<Task> _clients = new();

    public HubServer(int port, IMessageHandler handler)
    {
        _port = port;
        _handler = handler;
    }

    public Task StartAsync()
    {
        _listener = new TcpListener(IPAddress.Any, _port);
        _listener.Start();
        _cts = new CancellationTokenSource();
        _ = AcceptLoopAsync(_cts.Token);
        return Task.CompletedTask;
    }

    private async Task AcceptLoopAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested)
        {
            TcpClient client;
            try
            {
                client = await _listener!.AcceptTcpClientAsync(ct);
            }
            catch
            {
                break;
            }

            var task = HandleClientAsync(client, ct);
            _clients.Add(task);
        }
    }

    private async Task HandleClientAsync(TcpClient client, CancellationToken ct)
    {
        try
        {
            using var stream = client.GetStream();
            await HandshakeAsync(stream, ct);
            await WebSocketLoopAsync(stream, ct);
        }
        catch
        {
            // conexión cerrada o handshake inválido
        }
        finally
        {
            try { client.Dispose(); } catch { }
        }
    }

    /// <summary>Realiza el handshake HTTP → 101 Switching Protocols del WebSocket.</summary>
    private static async Task HandshakeAsync(NetworkStream stream, CancellationToken ct)
    {
        // Leer el request HTTP (hasta \r\n\r\n)
        var requestBytes = new List<byte>();
        var buffer = new byte[1024];
        using (var timeout = CancellationTokenSource.CreateLinkedTokenSource(ct))
        {
            timeout.CancelAfter(10000);
            while (!HasHeaderTerminator(requestBytes))
            {
                int n = await stream.ReadAsync(buffer, timeout.Token);
                if (n == 0) throw new EndOfStreamException("Cliente cerró durante el handshake");
                requestBytes.AddRange(buffer.Take(n));
            }
        }

        var requestText = Encoding.UTF8.GetString(requestBytes.ToArray());
        var key = ExtractWebSocketKey(requestText);
        if (key is null)
            throw new InvalidDataException("No se encontró Sec-WebSocket-Key");

        var accept = ComputeAcceptKey(key);
        var response =
            "HTTP/1.1 101 Switching Protocols\r\n" +
            "Upgrade: websocket\r\n" +
            "Connection: Upgrade\r\n" +
            "Sec-WebSocket-Accept: " + accept + "\r\n\r\n";

        var responseBytes = Encoding.ASCII.GetBytes(response);
        await stream.WriteAsync(responseBytes, ct);
    }

    private static bool HasHeaderTerminator(List<byte> data)
    {
        for (int i = 0; i < data.Count - 3; i++)
            if (data[i] == 13 && data[i + 1] == 10 && data[i + 2] == 13 && data[i + 3] == 10)
                return true;
        return false;
    }

    private static string? ExtractWebSocketKey(string request)
    {
        foreach (var line in request.Split("\r\n"))
        {
            if (line.StartsWith("Sec-WebSocket-Key:", StringComparison.OrdinalIgnoreCase))
                return line["Sec-WebSocket-Key:".Length..].Trim();
        }
        return null;
    }

    private static string ComputeAcceptKey(string key)
    {
        const string magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        using var sha1 = SHA1.Create();
        var hash = sha1.ComputeHash(Encoding.UTF8.GetBytes(key + magic));
        return Convert.ToBase64String(hash);
    }

    /// <summary>Lee frames de WebSocket y los entrega como texto completo al handler.</summary>
    private async Task WebSocketLoopAsync(NetworkStream stream, CancellationToken ct)
    {
        var inBuffer = new byte[64 * 1024];
        var pending = new List<byte>();

        while (!ct.IsCancellationRequested)
        {
            int n = await stream.ReadAsync(inBuffer, ct);
            if (n == 0) break;

            pending.AddRange(inBuffer.Take(n));

            // Procesar todos los mensajes completos que haya en el buffer pendiente
            foreach (var message in ExtractMessages(pending))
            {
                _handler.HandleMessage(message).GetAwaiter().GetResult();
            }
        }
    }

    /// <summary>
    /// Extrae mensajes completos (frames con FIN=1 y opcode texto/binario) de un buffer
    /// pendiente, dejando en el buffer cualquier byte sobrante de un frame incompleto.
    /// Devuelve false cuando el buffer pendiente queda sin mensajes completos.
    /// </summary>
    private static IEnumerable<string> ExtractMessages(List<byte> pending)
    {
        var messages = new List<string>();
        int offset = 0;
        int count = pending.Count;

        while (count - offset >= 2)
        {
            byte b1 = pending[offset];
            byte b2 = pending[offset + 1];
            bool fin = (b1 & 0x80) != 0;
            int opcode = b1 & 0x0F;
            bool masked = (b2 & 0x80) != 0;
            long payloadLen = b2 & 0x7F;

            int headerLen = 2;
            if (payloadLen == 126)
            {
                if (count - offset < 4) break;
                payloadLen = (pending[offset + 2] << 8) | pending[offset + 3];
                headerLen = 4;
            }
            else if (payloadLen == 127)
            {
                if (count - offset < 10) break;
                payloadLen = 0;
                for (int i = 0; i < 8; i++)
                    payloadLen = (payloadLen << 8) | pending[offset + 2 + i];
                headerLen = 10;
            }

            byte[] maskKey = new byte[4];
            if (masked)
            {
                if (count - offset < headerLen + 4) break;
                Array.Copy(pending.ToArray(), offset + headerLen, maskKey, 0, 4);
                headerLen += 4;
            }

            if (count - offset < headerLen + payloadLen) break;

            byte[] payload = new byte[payloadLen];
            Array.Copy(pending.ToArray(), offset + headerLen, payload, 0, (int)payloadLen);
            if (masked)
            {
                for (int i = 0; i < payloadLen; i++)
                    payload[i] = (byte)(payload[i] ^ maskKey[i % 4]);
            }

            offset += headerLen + (int)payloadLen;

            if (opcode == 0x1 || opcode == 0x2)
            {
                if (fin)
                {
                    messages.Add(Encoding.UTF8.GetString(payload));
                }
            }
            else if (opcode == 0x8) // close
            {
                break;
            }
        }

        // Eliminar del buffer pendiente los bytes ya procesados.
        // Si no se avanzó (frame incompleto), conservar el pendiente tal cual.
        if (offset > 0)
        {
            pending.RemoveRange(0, offset);
        }

        return messages;
    }

    public async ValueTask DisposeAsync()
    {
        _cts.Cancel();
        try { _listener?.Stop(); } catch { }
        try { await Task.WhenAll(_clients); } catch { }
    }
}
