package com.utilities.connection

import kotlinx.coroutines.flow.Flow

interface DeviceConnector {
    val discoveredDevices: Flow<List<DeviceInfo>>
    val isDiscovering: Flow<Boolean>
    
    suspend fun startDiscovery()
    suspend fun stopDiscovery()
    suspend fun getPairedDevices(): List<DeviceInfo>
}
