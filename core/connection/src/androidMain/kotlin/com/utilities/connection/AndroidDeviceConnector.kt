package com.utilities.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidDeviceConnector(private val context: Context) : DeviceConnector {
    private val _discoveredDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    override val discoveredDevices: Flow<List<DeviceInfo>> = _discoveredDevices.asStateFlow()
    
    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering: Flow<Boolean> = _isDiscovering.asStateFlow()
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val discoveredDevicesList = mutableListOf<DeviceInfo>()
    
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val deviceInfo = DeviceInfo(
                            name = it.name ?: "Desconocido",
                            address = it.address,
                            type = ConnectionType.BLUETOOTH,
                            isAvailable = true
                        )
                        if (discoveredDevicesList.none { d -> d.address == deviceInfo.address }) {
                            discoveredDevicesList.add(deviceInfo)
                            _discoveredDevices.value = discoveredDevicesList.toList()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isDiscovering.value = false
                }
            }
        }
    }
    
    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)
    }
    
    override suspend fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
        _isDiscovering.value = true
    }
    
    override suspend fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        _isDiscovering.value = false
    }
    
    override suspend fun getPairedDevices(): List<DeviceInfo> {
        return bluetoothAdapter?.bondedDevices?.map { device ->
            DeviceInfo(
                name = device.name ?: "Desconocido",
                address = device.address,
                type = ConnectionType.BLUETOOTH,
                isAvailable = true
            )
        } ?: emptyList()
    }
    
    fun destroy() {
        context.unregisterReceiver(receiver)
    }
}
