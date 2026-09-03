package com.utilities.steering

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.utilities.connection.ConnectionManager
import com.utilities.connection.ConnectionManagerFactory
import com.utilities.connection.ConnectionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Implementación Android del volante: lee el sensor de rotación (giroscopio)
 * y envía el estado al Hub por WebSocket.
 */
class AndroidSteeringController(
    private val context: Context
) : SteeringController, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val connection: ConnectionManager = ConnectionManagerFactory.create(ConnectionType.WIFI)

    override val connectionState
        get() = connection.connectionState

    private val _steeringState = MutableStateFlow(SteeringPayload(0f, 0f, 0f))
    val steeringState: StateFlow<SteeringPayload> = _steeringState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    override suspend fun connect(host: String, port: Int) {
        connection.connect(host, port)
        startSensor()
    }

    override suspend fun disconnect() {
        stopSensor()
        connection.disconnect()
    }

    override fun send(payload: SteeringPayload) {
        val message = SteeringController.buildMessage(payload)
        scope.launch {
            try {
                connection.send(message)
            } catch (_: Exception) {
            }
        }
    }

    private fun startSensor() {
        stopSensor()
        if (rotationSensor == null) return
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun stopSensor() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientation)

        val yaw = (orientation[0] * 180 / Math.PI).toFloat()
        val pitch = (orientation[1] * 180 / Math.PI).toFloat()
        val roll = (orientation[2] * 180 / Math.PI).toFloat()

        val state = SteeringPayload(roll = roll, pitch = pitch, yaw = yaw)
        _steeringState.value = state
        send(state)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun release() {
        stopSensor()
        scope.cancel()
    }
}
