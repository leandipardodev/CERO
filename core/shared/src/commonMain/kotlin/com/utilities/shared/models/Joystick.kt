package com.utilities.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class JoystickState(
    val axes: List<Float>,
    val buttons: List<Boolean>
)

@Serializable
data class JoystickConfig(
    val name: String,
    val axisCount: Int,
    val buttonCount: Int,
    val axisDeadZone: Float = 0.1f
)
