# Diálogo Resumen - Utilities App

## Marca
- **Nombre:** CERO - Remote PC Tools
- **Tagline:** "Tu PC, CERO distancia"
- **Nota:** Verificar marca "CERO" en clase 9 antes de publicar

## Repositorio
- **GitHub:** https://github.com/leandipardodev/CERO (público)
- **Ruta local:** `Documentos/proyectos compartido/CERO`
- **Rama:** master

## Equipo / Herramientas
- **Stack:** Kotlin Multiplatform
- **Android Studio:** 2026.1 instalado
- **Leand:** frontend/UI + iOS (no sabe android aún)
- **Hermano:** backend/conexiones + Android

## Utilidades
1. Transferencia archivos - Bidireccional, WiFi+BT, medianos
2. Joystick virtual - Configurable, competitive, baja latencia
3. Parlante BT - Audio sistema, balance calidad/latencia
4. Volante - 3 ejes giroscopio, configurable
5. Micrófono - Voz, filtro ruido

## División
- **Hermano:** Backend/conexiones + Android
- **Leand:** Frontend/UI + iOS

## Estado Actual

### Estructura Creada
```
CERO/
├── app/android/          → App Android (Compose)
├── core/
│   ├── connection/       → Conexiones WiFi/Bluetooth ✅
│   └── shared/           → Modelos compartidos ✅
├── feature/
│   ├── filetransfer/     → Pendiente
│   ├── joystick/         → Pendiente
│   ├── speaker/          → Pendiente
│   ├── steering/         → Pendiente
│   └── mic/              → Pendiente
```

### :core:connection (Completado)
- `ConnectionType` - WIFI, BLUETOOTH, AUTO
- `ConnectionState` - Disconnected, Connecting, Connected, Error
- `ConnectionManager` - Interfaz: connect(), disconnect(), send(), receivedData
- `DeviceInfo` - Modelo de dispositivo
- `DeviceConnector` - Interfaz para descubrir dispositivos
- `WifiConnectionManager` - WebSocket con Ktor
- `BluetoothConnectionManager` - expect/actual
- `AndroidDeviceConnector` - Descubrimiento BT en Android
- `PlatformConnection` - Helper Android

### :core:shared (Completado)
- `UtilityType` - Tipos de utilidad
- `Message` - Command, Response, Data
- `FileMetadata`, `FileChunk` - Archivos
- `JoystickState`, `JoystickConfig` - Joystick
- `AudioConfig`, `AudioChunk` - Audio
- `SteeringState`, `SteeringConfig` - Volante
- `MicrophoneConfig` - Micrófono
- `Protocol` - Serialización JSON

### UI Android (Completado)
- `HomeScreen` - Lista de utilidades con navegación
- Navegación con NavHost/NavController
- Pantallas placeholder para cada utilidad
- Agregada dependencia navigation-compose

## Próximos Pasos
1. Build en Android Studio (Ctrl+F9)
2. Crear emulador (Device Manager → Pixel 7 → Android 34)
3. Correr la app
4. Empezar features
5. Server PC (para recibir conexiones)
6. Permisos y conexión real

## CERO Hub (PC) - App Escritorio C#/.NET 10 WPF
```
hub/CeroHub/
├── CeroHub.slnx
├── CeroHub.Core/          → Lógica (WebSocket server, vJoy, protocolo)
└── CeroHub.App/           → UI WPF (estado servidor, IP, vJoy, log)
```
- **Servidor WebSocket** propio con TcpListener (no requiere privilegios admin).
- **vJoy**: wrapper a `vJoyInterfaceWrap.dll` (P/Invoke). Requiere driver vJoy instalado en la PC (github.com/BrunnerInnovation/vJoy).
- **UI WPF**: botón Iniciar/Detener, muestra IP del PC, estado vJoy, y log de mensajes del celular.
- **Validado end-to-end**: celular (JSON) → Hub → vJoy funcionando vía WebSocket.

## Volante en el celular (Sesión 13)
- `feature/steering`: `AndroidSteeringController` lee giroscopio (rotation vector), calcula roll/pitch/yaw y envía a la PC.
- UI `SteeringScreen`: IP del PC + Conectar, visual del volante, valores en vivo.
- Formato: `{"type":"Steering","action":"UPDATE","payload":{"roll":...,"pitch":...,"yaw":...}}`.
- **Para probar el volante**: PC con vJoy instalado + Hub corriendo; celular conectado a la misma WiFi + app.

> ⚠️ **Ojo:** hay una BITACORA.md con el diálogo completo en orden.
