package com.utilities.app.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Utility(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val utilities = listOf(
    Utility(
        id = "filetransfer",
        name = "Transferencia de Archivos",
        description = "Enviar y recibir archivos",
        icon = Icons.Default.FileCopy,
        route = "filetransfer"
    ),
    Utility(
        id = "joystick",
        name = "Joystick Virtual",
        description = "Control remoto para juegos",
        icon = Icons.Default.Gamepad,
        route = "joystick"
    ),
    Utility(
        id = "speaker",
        name = "Parlante Bluetooth",
        description = "Audio de PC en el celular",
        icon = Icons.Default.Speaker,
        route = "speaker"
    ),
    Utility(
        id = "steering",
        name = "Volante",
        description = "Control con giroscopio",
        icon = Icons.Default.DirectionsCar,
        route = "steering"
    ),
    Utility(
        id = "mic",
        name = "Micrófono Remoto",
        description = "Usar micrófono del celular",
        icon = Icons.Default.Mic,
        route = "mic"
    )
)
