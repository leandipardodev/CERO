package com.utilities.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class SteeringState(
    val roll: Float,
    val pitch: Float,
    val yaw: Float
)

@Serializable
data class SteeringConfig(
    val sensitivity: Float = 1.0f,
    val deadZone: Float = 0.05f,
    val invertX: Boolean = false,
    val invertY: Boolean = false
)
