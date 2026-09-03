package com.utilities.connection

import kotlinx.coroutines.flow.Flow

interface ConnectionManager {
    val connectionState: Flow<ConnectionState>
    val receivedData: Flow<ByteArray>
    
    suspend fun connect(host: String, port: Int, type: ConnectionType = ConnectionType.AUTO)
    suspend fun disconnect()
    suspend fun send(data: ByteArray)
    suspend fun send(data: String)
}
