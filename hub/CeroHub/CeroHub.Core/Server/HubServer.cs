using System.Net;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using CeroHub.Core.Protocol;

namespace CeroHub.Core.Server;

/// <summary>
/// Servidor WebSocket que recibe datos del celular (CERO app) y los
/// reenvía a los manejadores de utilidades correspondientes.
/// </summary>
public sealed class HubServer : IAsyncDisposable
{
    private readonly int _port;
    private readonly IMessageHandler _handler;
    private HttpListener? _listener;
    private CancellationTokenSource _cts = new();

    public HubServer(int port, IMessageHandler handler)
    {
        _port = port;
        _handler = handler;
    }

    public Task StartAsync()
    {
        _listener = new HttpListener();
        _listener.Prefixes.Add($"http://*:{_port}/");
        _listener.Start();
        _cts = new CancellationTokenSource();
        _ = AcceptLoopAsync(_cts.Token);
        return Task.CompletedTask;
    }

    private async Task AcceptLoopAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested)
        {
            HttpListenerContext ctx;
            try
            {
                ctx = await _listener!.GetContextAsync().WaitAsync(ct);
            }
            catch
            {
                break;
            }

            if (ctx.Request.IsWebSocketRequest)
            {
                _ = HandleWebSocketAsync(ctx, ct);
            }
            else
            {
                ctx.Response.StatusCode = 200;
                var bytes = Encoding.UTF8.GetBytes("CERO Hub");
                await ctx.Response.OutputStream.WriteAsync(bytes, ct);
                ctx.Response.Close();
            }
        }
    }

    private async Task HandleWebSocketAsync(HttpListenerContext ctx, CancellationToken ct)
    {
        WebSocket webSocket;
        try
        {
            var wsCtx = await ctx.AcceptWebSocketAsync(null);
            webSocket = wsCtx.WebSocket;
        }
        catch
        {
            return;
        }

        var buffer = new byte[1024 * 64];
        try
        {
            while (webSocket.State == WebSocketState.Open)
            {
                using var ms = new MemoryStream();
                WebSocketReceiveResult result;
                do
                {
                    result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), ct);
                    ms.Write(buffer, 0, result.Count);
                } while (!result.EndOfMessage);

                if (result.MessageType == WebSocketMessageType.Close)
                {
                    await webSocket.CloseAsync(WebSocketCloseStatus.NormalClosure, "bye", ct);
                    break;
                }

                var json = Encoding.UTF8.GetString(ms.ToArray());
                _handler.HandleMessage(json).GetAwaiter().GetResult();
            }
        }
        catch
        {
            // conexión cerrada
        }
        finally
        {
            try { webSocket.Dispose(); } catch { }
        }
    }

    public async ValueTask DisposeAsync()
    {
        _cts.Cancel();
        try { _listener?.Stop(); } catch { }
        _listener?.Close();
    }
}
