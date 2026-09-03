package com.utilities.connection

import android.content.Context

class PlatformConnection(private val context: Context) {
    fun createDeviceConnector(): DeviceConnector {
        return AndroidDeviceConnector(context)
    }
    
    fun createBluetoothConnectionManager(): BluetoothConnectionManager {
        return BluetoothConnectionManager()
    }
}
