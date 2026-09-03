# Utilities App - Estructura del Proyecto

## Arquitectura KMP

```
utilities-app/
├── app/
│   └── android/                    # App Android
├── core/
│   ├── connection/                 # Módulo de conexiones (WiFi/Bluetooth)
│   └── shared/                     # Modelos y protocolos compartidos
├── feature/
│   ├── filetransfer/               # Transferencia de archivos
│   ├── joystick/                   # Joystick virtual
│   ├── speaker/                    # Parlante Bluetooth
│   ├── steering/                   # Volante (giroscopio)
│   └── mic/                        # Micrófono remoto
└── gradle/
    └── libs.versions.toml          # Version catalog
```

## Módulos Core

### :core:connection
- Conexión WiFi (TCP/WebSocket)
- Conexión Bluetooth
- Gestión de dispositivos

### :core:shared
- Modelos de datos
- Protocolos de comunicación
- Utilidades comunes

## Módulos Feature

Cada feature es un módulo independiente que depende de core:
- `:feature:filetransfer` → Bidireccional, WiFi/BT
- `:feature:joystick` → Configurable, baja latencia
- `:feature:speaker` → Audio del sistema
- `:feature:steering` → 3 ejes, giroscopio
- `:feature:mic` → Voz, filtro de ruido

## División de Trabajo

- **Hermano:** Backend/conexiones + Android
- **Tú:** Frontend/UI + iOS

## Próximos Pasos

1. Implementar `:core:connection` con TCP WebSocket
2. Implementar `:core:shared` con modelos
3. Desarrollar cada feature por separado
4. Crear UI para Android
5. Crear UI para iOS
