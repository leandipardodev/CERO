package com.utilities.connection

data class DeviceInfo(
    val name: String,
    val address: String,
    val type: ConnectionType,
    val isAvailable: Boolean
)
