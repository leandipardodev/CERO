# Diálogo Resumen - Utilities App

## Marca
- **Nombre:** CERO - Remote PC Tools
- **Tagline:** "Tu PC, CERO distancia"
- **Nota:** Verificar marca "CERO" en clase 9 antes de publicar

## Contexto
- **Proyecto:** App iOS/Android de utilidades para PC
- **Stack:** Kotlin Multiplatform
- **Equipo:** Leand (frontend/iOS) + Hermano (backend/conexiones/Android)

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
utilities-app/
├── app/android/          → App Android (Compose)
├── core/
│   ├── connection/       → Conexiones WiFi/Bluetooth ✅
│   └── shared/           → Pendiente
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
1. Empezar features
2. Server PC (para recibir conexiones)
3. Permisos y conexión real
