# BITACORA - CERO Remote PC Tools

Bitácora completa del diálogo. Hiper-resumida, en orden. Actualizar al final de cada sesión.

---

## [Sesión 1] Planificación del proyecto

**User:** Quiere app iOS/Android de utilidades PC. Organizada + resúmenes breves + sugerencias de planificación. Yo + hermano usando opencode → modular para no pisarse.

**Utilidades (definidas):**
1. Transferencia de archivos
2. Joystick virtual
3. Parlante Bluetooth (audio PC → celular)
4. Volante PC (giroscopio celular)
5. Micrófono remoto (mic del celular)

**Stack:** Asist preguntó stack → Leand preguntó recomendación.
- Recomendado: Kotlin Multiplatform (lógica compartida + UI nativa, ideal por Bluetooth/TCP).
- Leand eligió **Kotlin**.

**División:** Hermano = conexiones/backend + Android. Leand = frontend/UI + iOS. Utilidades se reparten ellos.

**Detalle por pregunta (interfaz):**
- Transferencia: **Bidireccional, WiFi+BT, archivos medianos**
- Joystick: **Configurable por usuario, juegos competitive (baja latencia)**
- Parlante: **Todo el audio del sistema, balance calidad/latencia**
- Volante: **Uso general, 3 ejes configurables**
- Micrófono: **Voz, filtro de ruido**

## [Sesión 2] Creación de estructura

Creé el proyecto KMP (Gradle multiplatform):
```
utilities-app/
├── app/android/          → App Android Compose
├── core/connection/      → conexiones
├── core/shared/          → modelos
├── feature/{filetransfer,joystick,speaker,steering,mic}/
└── gradle/libs.versions.toml
```

Módulos core/features/app con build.gradle.kts (KMP + Android + iOS targets).

## [Sesión 3] Module :core:connection (implementado)

- `ConnectionType` - WIFI/BLUETOOTH/AUTO
- `ConnectionState` - Disconnected/Connecting/Connected/Error
- `ConnectionManager` - interfaz connect/disconnect/send/receivedData
- `DeviceInfo`, `DeviceConnector` - descubrir dispositivos
- `WifiConnectionManager` - WebSocket con Ktor
- `BluetoothConnectionManager` - expect/actual (Android: RFCOMM socket)
- `AndroidDeviceConnector` - discovery BT
- `PlatformConnection` - helper Android

## [Sesión 4] Module :core:shared (implementado)

- `UtilityType` - tipos de utilidad
- `Message` - Command/Response/Data
- `FileMetadata`, `FileChunk`
- `JoystickState`, `JoystickConfig`
- `AudioConfig`, `AudioChunk`
- `SteeringState`, `SteeringConfig`
- `MicrophoneConfig`
- `Protocol` - serialización JSON

**Docs creados:** ANDROID_GUIDE.md (guía Android), DIALOGUE.md (resumen hermano).

## [Sesión 5] UI Android (Compose)

- `HomeScreen` - lista de utilidades con iconos + navegación
- `NavGraph` - NavHost/navegación entre pantallas
- 5 pantallas placeholder (FileTransfer, Joystick, Speaker, Steering, Mic)
- Dependencia `navigation-compose` agregada
- MainActivity usa NavGraph

## [Sesión 6] Naming / Marca

- User pidió nombre con buen SEO AppStore/PlayStore.
- Opciones dadas → **RemotePC Tools** ya existe (RemotePC real) → descartado.
- User quiso nombre corto (Klip/Boobaa) + subtítulo SEO "Remote PC Tools".
- User pidió siglas relacionadas (ES/EN).
- **Decisión: "CERO"** + subtítulo **"CERO - Remote PC Tools"**, tagline *"Tu PC, CERO distancia"*.
- **Legal:** hay marcas CERO (bicicletas, golf, construcción) pero **1 en clase 9 (electrónica/software, Shenzhen)** = riesgo posible de conflicto. Recomendado verificar clase 9 en INPI país antes de publicar o usar "CERO PC".

## [Sesión 7] Reubicación del proyecto

- Proyecto estaba anidado dentro de `web logic`. Movido a: `Documentos/proyectos compartido/CERO`.
- Eliminada carpentería vacía. Solicitado reabrir proyecto en Android Studio desde la nueva ruta.

## [Sesión 8] Repositorio GitHub

- `git init` + `.gitignore` + commit inicial ("Inicial: estructura KMP..." 50 files).
- `gh repo create CERO --private` → creado, luego cambiado a **público**.
- **Repo:** https://github.com/leandipardodev/CERO (rama master).

## [Sesión 9] Herramientas / Testeo

- Android Studio instalado (winget, 2026.1) en `C:\Program Files\Android\Android Studio`.
- Guía a Leand (no sabe Android):
  - Build → Make Project (Ctrl+F9), esperar BUILD SUCCESSFUL
  - Device Manager → + → Pixel 7 → descargar Android 34
  - Play verde (▶) para correr la app en emulador.

## [Sesión 10] Documentación

- User pidió documentar TODO (bitácora completa).
- `DIALOGUE.md` = resumen corto para hermano (actualizado).
- `BITACORA.md` = bitácora completa del diálogo (este archivo).

---

## Estado Actual
- Estructura KMP ✅
- :core:connection ✅
- :core:shared ✅
- UI Android (home + placeholders) ✅
- Repo GitHub público ✅
- Android Studio instalado ✅
- **Pendiente:** build/emulador en Android Studio, server PC, features, memoria de permisos.

## Próximos Pasos (técnicos)
1. Build (Ctrl+F9) y correr app en emulador
2. Server PC (para que la PC reciba conexiones)
3. Implementar features una a una
4. Permisos y conexión real
