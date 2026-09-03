package com.utilities.shared.protocols

import com.utilities.shared.models.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Protocol {
    private val json = Json { ignoreUnknownKeys = true }
    
    fun encode(message: Message): ByteArray {
        return json.encodeToString(message).toByteArray()
    }
    
    fun decode(data: ByteArray): Message {
        return json.decodeFromString(data.decodeToString())
    }
    
    fun decode(data: String): Message {
        return json.decodeFromString(data)
    }
}
