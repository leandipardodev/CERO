using System.Net;
using System.Net.Sockets;
using System.Windows;
using System.Windows.Media;
using CeroHub.Core.Server;
using CeroHub.Core.VJoy;

namespace CeroHub.App;

public partial class MainWindow : Window
{
    private HubServer? _server;
    private MessageHandler? _handler;
    private readonly VJoyDriver _vjoyProbe = new();

    public MainWindow()
    {
        InitializeComponent();
        Loaded += MainWindow_Loaded;
    }

    private void MainWindow_Loaded(object sender, RoutedEventArgs e)
    {
        TxtAddress.Text = $"IP del PC: {GetLocalIp() ?? "desconocida"} · puerto 8080";
        TxtVJoy.Text = _vjoyProbe.IsAvailable
            ? "vJoy: instalado y disponible ✓"
            : "vJoy: NO instalado. Descargar desde github.com/BrunnerInnovation/vJoy";
        TxtVJoy.Foreground = _vjoyProbe.IsAvailable ? Brushes.ForestGreen : Brushes.DarkOrange;
    }

    private static string? GetLocalIp()
    {
        try
        {
            using var socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            socket.Connect("8.8.8.8", 80);
            return (socket.LocalEndPoint as IPEndPoint)?.Address.ToString();
        }
        catch
        {
            return null;
        }
    }

    private void BtnStart_Click(object sender, RoutedEventArgs e)
    {
        if (_server is null)
        {
            _handler = new MessageHandler();
            _handler.Log += (_, msg) => AddLog(msg);

            _server = new HubServer(8080, _handler);
            _server.StartAsync();
            TxtStatus.Text = "Corriendo en ws://0.0.0.0:8080";
            TxtStatus.Foreground = Brushes.ForestGreen;
            BtnStart.Content = "Detener";
            AddLog("Servidor iniciado en puerto 8080");
        }
        else
        {
            _server.DisposeAsync().AsTask().GetAwaiter().GetResult();
            _server = null;
            TxtStatus.Text = "Detenido";
            TxtStatus.Foreground = Brushes.Firebrick;
            BtnStart.Content = "Iniciar";
            AddLog("Servidor detenido");
        }
    }

    private void AddLog(string message)
    {
        Dispatcher.Invoke(() =>
        {
            LogList.Items.Add($"[{DateTime.Now:HH:mm:ss}] {message}");
            if (message.StartsWith("Volante"))
                TxtSteering.Text = message;
            LogList.Items.MoveCurrentToLast();
        });
    }

    private void BtnClear_Click(object sender, RoutedEventArgs e)
    {
        LogList.Items.Clear();
    }
}
