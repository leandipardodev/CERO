package com.utilities.connection

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable

class WifiConnectionManager : ConnectionManager {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedData = MutableSharedFlow<ByteArray>()
    override val receivedData: Flow<ByteArray> = _receivedData.asSharedFlow()
    
    private var client: HttpClient? = null
    private var session: WebSocketSession? = null
    private var receiveJob: Job? = null
    
    override suspend fun connect(host: String, port: Int, type: ConnectionType) {
        try {
            _connectionState.value = ConnectionState.Connecting
            
            client = HttpClient {
                install(WebSockets)
            }
            
            session = client!!.webSocketSession(host = host, port = port)
            
            _connectionState.value = ConnectionState.Connected
            
            startReceiving()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Error de conexión")
            client?.close()
            client = null
        }
    }
    
    override suspend fun disconnect() {
        receiveJob?.cancel()
        session?.close()
        session = null
        client?.close()
        client = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    override suspend fun send(data: ByteArray) {
        session?.send(Frame.Binary(true, data))
    }
    
    override suspend fun send(data: String) {
        session?.send(Frame.Text(data))
    }
    
    private fun startReceiving() {
        receiveJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                for (frame in session!!.incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val bytes = frame.readBytes()
                            _receivedData.emit(bytes)
                        }
                        is Frame.Text -> {
                            val text = frame.readText()
                            _receivedData.emit(text.toByteArray())
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(e.message ?: "Error recibiendo datos")
            }
        }
    }
}
