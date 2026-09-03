package com.utilities.steering

import kotlinx.serialization.Serializable

@Serializable
data class SteeringPayload(
    val roll: Float,
    val pitch: Float,
    val yaw: Float
)
