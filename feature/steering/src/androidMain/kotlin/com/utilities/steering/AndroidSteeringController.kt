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
import kotlin.math.sqrt

/**
 * Implementación Android del volante.
 *
 * Usa dos sensores:
 *  - RotationVector: para obtener roll/pitch/yaw (giro del volante).
 *  - Gravity: para detectar la pose física del dispositivo
 *    (horizontal, vertical, de costado, etc.).
 *
 * Ejemplos de pose basados en el vector de gravedad (valores en m/s²,
 * signo según el eje del dispositivo):
 *  - FLAT_BACK   : pantalla hacia arriba  (gZ ≈ +9.8)
 *  - FLAT_FRONT  : pantalla hacia abajo   (gZ ≈ -9.8)
 *  - PORTRAIT    : vertical normal        (gY ≈ +9.8)
 *  - UPSIDE_DOWN : vertical invertido     (gY ≈ -9.8)
 *  - LANDSCAPE   : de costado             (gX ≈ ±9.8)
 */
class AndroidSteeringController(
    private val context: Context
) : SteeringController, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val connection: ConnectionManager = ConnectionManagerFactory.create(ConnectionType.WIFI)

    override val connectionState
        get() = connection.connectionState

    private val _steeringState = MutableStateFlow(SteeringPayload(0f, 0f, 0f))
    val steeringState: StateFlow<SteeringPayload> = _steeringState.asStateFlow()

    private val _orientation = MutableStateFlow(DeviceOrientation.UNKNOWN)
    val orientation: StateFlow<DeviceOrientation> = _orientation.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Gravedad detectada en los ejes del dispositivo
    private var gx = 0f
    private var gy = 0f
    private var gz = 0f
    private var hasGravity = false

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

    fun startSensor() {
        stopSensor()
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        }
        if (gravitySensor != null) {
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensor() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GRAVITY -> {
                gx = event.values[0]
                gy = event.values[1]
                gz = event.values[2]
                hasGravity = true
                _orientation.value = detectOrientation()
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val yaw = (orientationAngles[0] * 180 / Math.PI).toFloat()
                val pitch = (orientationAngles[1] * 180 / Math.PI).toFloat()
                val roll = (orientationAngles[2] * 180 / Math.PI).toFloat()

                val orientation = if (hasGravity) _orientation.value else DeviceOrientation.UNKNOWN
                val state = SteeringPayload(
                    roll = roll,
                    pitch = pitch,
                    yaw = yaw,
                    orientation = orientation
                )
                _steeringState.value = state
                send(state)
            }
        }
    }

    /**
     * Detecta la pose del dispositivo a partir del vector de gravedad.
     * Se comparan las magnitudes de cada componente para decidir el eje dominante.
     */
    private fun detectOrientation(): DeviceOrientation {
        val magnitude = sqrt(gx * gx + gy * gy + gz * gz)
        if (magnitude < 1f) return DeviceOrientation.UNKNOWN

        val nx = gx / magnitude
        val ny = gy / magnitude
        val nz = gz / magnitude

        // Gravedad apunta hacia el centro de la Tierra.
        // El vector de gravedad en coordenadas del dispositivo indica cómo está
        // sostenido. Comparamos la magnitud absoluta de cada componente.
        val ax = kotlin.math.abs(nx)
        val ay = kotlin.math.abs(ny)
        val az = kotlin.math.abs(nz)

        return when {
            // Vertical (dominante Y)
            ay > ax && ay > az -> if (ny > 0) DeviceOrientation.PORTRAIT else DeviceOrientation.UPSIDE_DOWN
            // De costado (dominante X)
            ax > ay && ax > az -> if (nx > 0) DeviceOrientation.LANDSCAPE_RIGHT else DeviceOrientation.LANDSCAPE_LEFT
            // Plano (dominante Z)
            az > ax && az > ay -> if (nz > 0) DeviceOrientation.FLAT_BACK else DeviceOrientation.FLAT_FRONT
            else -> DeviceOrientation.UNKNOWN
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun release() {
        stopSensor()
        scope.cancel()
    }
}
