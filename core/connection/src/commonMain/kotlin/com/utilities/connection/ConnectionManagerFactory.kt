package com.utilities.connection

object ConnectionManagerFactory {
    fun create(type: ConnectionType): ConnectionManager {
        return when (type) {
            ConnectionType.WIFI -> WifiConnectionManager()
            ConnectionType.BLUETOOTH -> createBluetoothManager()
            ConnectionType.AUTO -> WifiConnectionManager()
        }
    }
    
    private fun createBluetoothManager(): BluetoothConnectionManager {
        return BluetoothConnectionManager()
    }
}
