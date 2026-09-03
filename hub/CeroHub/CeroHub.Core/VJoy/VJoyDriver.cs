using vJoyInterfaceWrap;

namespace CeroHub.Core.VJoy;

/// <summary>
/// Controlador de vJoy: crea un joystick virtual reconocido por Windows
/// y permite mover sus ejes y botones. Usa el dispositivo vJoy ID 1.
/// Requiere que el driver vJoy esté instalado en el sistema.
/// </summary>
public sealed class VJoyDriver : IDisposable
{
    private const long AxisMin = 0x0000;
    private const long AxisCenter = 0x4000;
    private const long AxisMax = 0x8000;

    private readonly vJoy _vjoy = new();
    private const uint DeviceId = 1;
    private bool _acquired;

    /// <summary>true si el driver vJoy está instalado y disponible.</summary>
    public bool IsAvailable
    {
        get
        {
            try
            {
                return _vjoy.vJoyEnabled();
            }
            catch
            {
                return false;
            }
        }
    }

    /// <summary>Adquiere el dispositivo virtual. Devuelve true si tuvo éxito.</summary>
    public bool Acquire()
    {
        if (!IsAvailable)
            return false;

        _acquired = _vjoy.AcquireVJD(DeviceId);
        if (_acquired)
        {
            // Resetear todos los ejes al centro para un estado limpio.
            Reset();
        }
        return _acquired;
    }

    /// <summary>Lleva todos los ejes al centro.</summary>
    public void Reset()
    {
        SetAxis(AxisCenter, AxisId.X);
        SetAxis(AxisCenter, AxisId.Y);
        SetAxis(AxisCenter, AxisId.Z);
    }

    public void SetAxis(float normalized, AxisId axis)
    {
        // normalized va de -1.0 a 1.0 → mapear a 0x0000..0x8000
        var clamped = Math.Clamp(normalized, -1f, 1f);
        var value = (long)(AxisCenter + clamped * 16384);
        value = Math.Clamp(value, AxisMin, AxisMax);

        var usage = axis switch
        {
            AxisId.X => HID_USAGES.HID_USAGE_X,
            AxisId.Y => HID_USAGES.HID_USAGE_Y,
            AxisId.Z => HID_USAGES.HID_USAGE_Z,
            _ => HID_USAGES.HID_USAGE_X
        };

        _vjoy.SetAxis((int)value, DeviceId, usage);
    }

    public void SetButton(bool state, int buttonIndex)
    {
        _vjoy.SetBtn(state, DeviceId, (uint)buttonIndex);
    }

    /// <summary>Libera el dispositivo virtual.</summary>
    public void Release()
    {
        if (_acquired)
        {
            _vjoy.RelinquishVJD(DeviceId);
            _acquired = false;
        }
    }

    public void Dispose() => Release();
}

public enum AxisId
{
    X,
    Y,
    Z
}
