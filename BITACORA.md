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

## [Sesión 11] Setup compilación (fixes)

- Error build: agente kotlin hang/no SDK → resuelto.
- **Versionado correcto final:** Gradle 8.9 + AGP 8.5.2 + Kotlin 2.0 + JDK17.
- JDK17 instalado (Temurin) en `C:\Program Files\Eclipse Adoptium`.
- Fix `expect class BluetoothConnectionManager` → patrón `expect fun`/`actual fun`.
- Iconos Material que faltaban → agregado `material-icons-extended`.
- Icono launcher creado (adaptive icon).
- Character `¿` inválido borrado del manifest; permisos/limpieza manifest.
- Regla: commit+push automático cuando cerca de cuota diaria.

## [Sesión 12] CERO Hub (PC) - App escritorio C#/.NET WPF

- User quería **1 solo software en PC** que sirva para todas las utilidades.
- **Decisión:** CERO Hub en **C#/.NET 10**, UI **WPF**.
- Estructura: `hub/CeroHub/` → `CeroHub.Core` (lógica) + `CeroHub.App` (WPF UI).
- **Core:** Protocol (modelos JSON), VJoyDriver (vJoy P/Invoke, paquete vJoyInterface 0.2.1.6), HubServer (WebSocket puerto 8080), MessageHandler (celular→vJoy).
- **UI:** estado servidor, IP PC, estado vJoy, log, muestra volante recibido.
- **vJoy:** driver se instala por separado (BrunnerInnovation fork 2.2.2.0).
- Compila: 0 errores.
- **Pendiente:** feature móvil `steering` (leer giroscopio y mandar al Hub), instalar vJoy, joystick/audio/transferencia en Hub.

## [Sesión 13] Volante en el celular + fixes Hub

- Implementado el **volante en el celular** (`feature/steering`):
  - `AndroidSteeringController`: lee `Sensor.TYPE_ROTATION_VECTOR`, calcula roll/pitch/yaw (ángulos de Euler), actualiza `steeringState` y envía el JSON al Hub por WebSocket.
  - `SteeringPayload` + `SteeringController.buildMessage()`: formato `{"type":"Steering","action":"UPDATE","payload":{...}}`.
  - UI `SteeringScreen`: campo IP + botón Conectar/Desconectar, visual de volante (arco con aguja), valores de giróscopo en vivo, estado de conexión.
- **Bug corregido** en `ConnectionManager`: el interface exponía `Flow<ConnectionState>` pero los implementadores usaban `StateFlow` → cambiado a `StateFlow<ConnectionState>` (la UI usa `collectAsState`).
- **Bug encontrado y corregido en el Hub**: `System.Text.Json` de .NET por defecto NO deserializa enums desde strings (solo números) → agregado `JsonStringEnumConverter(JsonNamingPolicy.CamelCase)` al `JsonDefaults.Options`. Sin esto, el Hub fallaba al parsear `"type":"Steering"`.
- **Bug del wrapper vJoy en .NET 10**: el paquete NuGet `vJoyInterface` (net20) no se resolvía en runtime → reemplazado por `<Reference>` directo a `vJoyInterfaceWrap.dll` + copia de la nativa `vJoyInterface.dll` al output (`PlatformTarget x64`). La WPF ahora carga el wrapper sin crashear.
- **Bug de permisos**: `HttpListener` con `http://*:8080/` requiere ACL http.sys (acceso denegado sin admin) → reemplazado por servidor WebSocket casero con `TcpListener` + handshake + frames (sin privilegios). 
- **Pruebas validadas**:
  - Protocolo: parseo de JSON del celular correcto (Steering, Ping, joystick case-insensitive).
  - WebSocket end-to-end: cliente se conecta, envía JSON, Hub loguea `Volante roll=25`, `Ping recibido`.
  - WPF arranca sin crashear (vJoy cargado, devuelve "no instalado" con gracia).
  - App Android + APK debug compilan (BUILD SUCCESSFUL).
- **Pendiente:** instalar driver vJoy en la PC para que el eje X se mueva, probar en el Samsung A14 vía USB.

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
