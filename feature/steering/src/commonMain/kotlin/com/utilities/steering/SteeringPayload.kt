package com.utilities.steering

import kotlinx.serialization.Serializable

/** Pose / orientación física del dispositivo. */
@Serializable
enum class DeviceOrientation {
    UNKNOWN,
    FLAT_BACK,       // acostado, pantalla hacia arriba
    FLAT_FRONT,      // acostado, pantalla hacia abajo
    PORTRAIT,        // vertical normal
    UPSIDE_DOWN,     // vertical invertido
    LANDSCAPE_LEFT,  // de costado, apuntando a la izquierda
    LANDSCAPE_RIGHT, // de costado, apuntando a la derecha
}

@Serializable
data class SteeringPayload(
    val roll: Float,
    val pitch: Float,
    val yaw: Float,
    val orientation: DeviceOrientation = DeviceOrientation.UNKNOWN
)
