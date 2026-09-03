using System.Text.Json;
using CeroHub.Core.Protocol;
using CeroHub.Core.VJoy;

namespace CeroHub.Core.Server;

public interface IMessageHandler
{
    event EventHandler<string>? Log;
    Task HandleMessage(string json);
}

/// <summary>
/// Recibe los mensajes JSON del celular y los aplica a las utilidades
/// correspondientes (volante → vJoy, etc.).
/// </summary>
public sealed class MessageHandler : IMessageHandler
{
    public event EventHandler<string>? Log;

    private readonly Lazy<VJoyDriver> _steeringVjoy = new(() =>
    {
        var d = new VJoyDriver();
        d.Acquire();
        return d;
    });

    public async Task HandleMessage(string json)
    {
        try
        {
            var options = new JsonSerializerOptions { PropertyNameCaseInsensitive = true };
            var msg = JsonSerializer.Deserialize<IncomingMessage>(json, options);
            if (msg is null)
                return;

            switch (msg.Type)
            {
                case MessageType.Steering:
                    HandleSteering(msg.Payload);
                    break;
                case MessageType.Ping:
                    Log?.Invoke(this, "Ping recibido");
                    break;
                default:
                    Log?.Invoke(this, $"Tipo no implementado aún: {msg.Type}");
                    break;
            }
        }
        catch (Exception ex)
        {
            Log?.Invoke(this, $"Error: {ex.Message}");
        }

        await Task.CompletedTask;
    }

    private void HandleSteering(string payload)
    {
        var options = new JsonSerializerOptions { PropertyNameCaseInsensitive = true };
        var state = JsonSerializer.Deserialize<SteeringState>(payload, options);
        if (state is null)
            return;

        var vjoy = _steeringVjoy.Value;
        // roll = giro del volante → eje X horizontal
        vjoy.SetAxis(state.Roll, AxisId.X);
        Log?.Invoke(this, $"Volante roll={state.Roll:F2}");
    }
}
