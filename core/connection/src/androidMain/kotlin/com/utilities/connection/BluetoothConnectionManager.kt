package com.utilities.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

actual fun createBluetoothConnectionManager(): ConnectionManager {
    return BluetoothConnectionManager()
}

class BluetoothConnectionManager : ConnectionManager {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedData = MutableSharedFlow<ByteArray>()
    override val receivedData: Flow<ByteArray> = _receivedData.asSharedFlow()
    
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var receiveJob: Job? = null
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    override suspend fun connect(host: String, port: Int, type: ConnectionType) {
        try {
            _connectionState.value = ConnectionState.Connecting
            
            val device = bluetoothAdapter?.getRemoteDevice(host)
                ?: throw Exception("Dispositivo no encontrado")
            
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            
            _connectionState.value = ConnectionState.Connected
            
            startReceiving()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Error de conexión Bluetooth")
            disconnect()
        }
    }
    
    override suspend fun disconnect() {
        receiveJob?.cancel()
        inputStream?.close()
        outputStream?.close()
        socket?.close()
        socket = null
        inputStream = null
        outputStream = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    override suspend fun send(data: ByteArray) {
        outputStream?.write(data)
        outputStream?.flush()
    }
    
    override suspend fun send(data: String) {
        send(data.toByteArray())
    }
    
    private fun startReceiving() {
        receiveJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            try {
                while (isActive) {
                    val bytes = inputStream?.read(buffer) ?: break
                    if (bytes > 0) {
                        val data = buffer.copyOf(bytes)
                        _receivedData.emit(data)
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(e.message ?: "Error recibiendo datos")
            }
        }
    }
}
