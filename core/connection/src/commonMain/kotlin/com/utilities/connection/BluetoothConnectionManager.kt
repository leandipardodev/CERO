package com.utilities.connection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

expect class BluetoothConnectionManager : ConnectionManager {
    override val connectionState: StateFlow<ConnectionState>
    override val receivedData: Flow<ByteArray>
    
    override suspend fun connect(host: String, port: Int, type: ConnectionType)
    override suspend fun disconnect()
    override suspend fun send(data: ByteArray)
    override suspend fun send(data: String)
}
