package com.utilities.connection

object ConnectionManagerFactory {
    fun create(type: ConnectionType): ConnectionManager {
        return when (type) {
            ConnectionType.WIFI -> WifiConnectionManager()
            ConnectionType.BLUETOOTH -> createBluetoothConnectionManager()
            ConnectionType.AUTO -> WifiConnectionManager()
        }
    }
}
