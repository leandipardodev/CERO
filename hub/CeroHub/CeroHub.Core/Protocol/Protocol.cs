using System.Text.Json;
using System.Text.Json.Serialization;

namespace CeroHub.Core.Protocol;

public static class JsonDefaults
{
    public static readonly JsonSerializerOptions Options = new()
    {
        PropertyNameCaseInsensitive = true
    };

    static JsonDefaults()
    {
        Options.Converters.Add(new JsonStringEnumConverter(JsonNamingPolicy.CamelCase));
    }
}

/// <summary>
/// Mensajes recibidos desde el celular.
/// El tipo determina qué utilidad genera el dato.
/// </summary>
public enum MessageType
{
    Steering,   // Volante (giroscopio)
    Joystick,   // Joystick virtual
    Audio,      // Audio del micrófono / parlante
    Transfer,   // Transferencia de archivos
    Ping
}

public record IncomingMessage(
    [property: JsonPropertyName("type")] MessageType Type,
    [property: JsonPropertyName("action")] string Action,
    [property: JsonPropertyName("payload")] JsonElement? Payload
);

/// <summary>Estado del volante enviado por el celular (giroscopio).</summary>
public record SteeringState(
    [property: JsonPropertyName("roll")] float Roll,
    [property: JsonPropertyName("pitch")] float Pitch,
    [property: JsonPropertyName("yaw")] float Yaw
);
