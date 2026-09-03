# Módulo :core:connection

## Archivos Creados

### commonMain (Compartido)
- `ConnectionType.kt` - Tipos de conexión (WIFI, BLUETOOTH, AUTO)
- `ConnectionState.kt` - Estados de conexión (Disconnected, Connecting, Connected, Error)
- `ConnectionManager.kt` - Interfaz principal para gestionar conexiones
- `DeviceInfo.kt` - Modelo de información de dispositivo
- `DeviceConnector.kt` - Interfaz para descubrir dispositivos
- `WifiConnectionManager.kt` - Implementación WiFi con Ktor WebSocket
- `BluetoothConnectionManager.kt` - Interfaz expect para Bluetooth
- `ConnectionManagerFactory.kt` - Factory para crear ConnectionManagers

### androidMain (Android)
- `BluetoothConnectionManager.kt` - Implementación Bluetooth para Android
- `AndroidDeviceConnector.kt` - Conector de dispositivos Bluetooth para Android
- `PlatformConnection.kt` - Helper para crear conectores en Android

## Uso

```kotlin
// Crear ConnectionManager
val wifiManager = ConnectionManagerFactory.create(ConnectionType.WIFI)
val bluetoothManager = ConnectionManagerFactory.create(ConnectionType.BLUETOOTH)

// Conectar
wifiManager.connect("192.168.1.100", 8080)

// Escuchar estado
wifiManager.connectionState.collect { state ->
    when (state) {
        is ConnectionState.Connected -> println("Conectado")
        is ConnectionState.Error -> println("Error: ${state.message}")
        else -> {}
    }
}

// Enviar datos
wifiManager.send("Hello PC".toByteArray())

// Recibir datos
wifiManager.receivedData.collect { data ->
    println("Recibido: ${data.decodeToString()}")
}
```

## Próximos Pasos

1. Implementar iOS Bluetooth (iosMain)
2. Agregar protocolos de autenticación
3. Agregar reconnect automático
4. Agregar manejo de errores mejorado
