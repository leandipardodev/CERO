# Módulo :core:shared

## Modelos Creados

### UtilityType.kt
- Tipos de utilidad: FILE_TRANSFER, JOYSTICK, SPEAKER, STEERING, MICROPHONE

### Message.kt
- `Command` - Comandos de la app al PC
- `Response` - Respuestas del PC a la app
- `Data` - Datos de utilidades

### FileTransfer.kt
- `FileMetadata` - Metadatos de archivo
- `FileChunk` - Chunk de archivo para transferencia

### Joystick.kt
- `JoystickState` - Estado del joystick (ejes + botones)
- `JoystickConfig` - Configuración del joystick

### Audio.kt
- `AudioConfig` - Configuración de audio
- `AudioChunk` - Chunk de audio

### Steering.kt
- `SteeringState` - Estado del volante (roll, pitch, yaw)
- `SteeringConfig` - Configuración del volante

### Microphone.kt
- `MicrophoneConfig` - Configuración del micrófono

### Protocol.kt
- `encode()` - Serializar Message a ByteArray
- `decode()` - Deserializar ByteArray a Message

## Uso
```kotlin
val command = Message.Command(
    type = UtilityType.JOYSTICK,
    action = "UPDATE",
    payload = Json.encodeToString(joystickState)
)
val bytes = Protocol.encode(command)
connectionManager.send(bytes)
```
