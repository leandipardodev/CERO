# Guía Rápida Android Studio

## Abrir Proyecto
1. Abrir Android Studio
2. File → Open → Seleccionar carpeta `utilities-app`
3. Esperar sincronización Gradle

## Estructura del Proyecto
```
utilities-app/
├── app/android/          → Tu app (Compose)
├── core/connection/      → Conexiones WiFi/BT
├── core/shared/          → Modelos compartidos
└── feature/*/            → Cada utilidad
```

## Comandos Útiles
- `Ctrl + Shift + R` - Run
- `Ctrl + Shift + F` - Search in files
- `Ctrl + Alt + O` - Organize imports

## Compose Basics
```kotlin
@Composable
fun MyScreen() {
    Column {
        Text("Hola")
        Button(onClick = { }) {
            Text("Click")
        }
    }
}
```

## Navegación
- `NavHost` para navegar entre pantallas
- `NavController` para controlar navegación
