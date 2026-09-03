package com.utilities.steering

import com.utilities.connection.ConnectionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

/**
 * Controlador del volante: conecta al Hub por WebSocket (core:connection)
 * y envía el estado del giroscopio cuando el sensor entrega datos.
 * La lectura del sensor la implementa la plataforma (androidMain etc.).
 */
interface SteeringController {
    /** Estado de la conexión Con el Hub. */
    val connectionState: StateFlow<ConnectionState>

    /** Conecta al Hub (host de la PC) y arranca la lectura del sensor. */
    suspend fun connect(host: String, port: Int)

    suspend fun disconnect()

    fun send(payload: SteeringPayload)

    companion object {
        private val json = Json { encodeDefaults = true }

        /** Serializa el payload y lo envuelve en el mensaje de protocolo del Hub. */
        fun buildMessage(payload: SteeringPayload): String =
            """{"type":"Steering","action":"UPDATE","payload":${json.encodeToString(SteeringPayload.serializer(), payload)}}"""
    }
}
