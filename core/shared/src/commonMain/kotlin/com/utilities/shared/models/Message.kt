package com.utilities.shared.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Message {
    @Serializable
    data class Command(
        val type: UtilityType,
        val action: String,
        val payload: String = ""
    ) : Message()
    
    @Serializable
    data class Response(
        val success: Boolean,
        val data: String = "",
        val error: String = ""
    ) : Message()
    
    @Serializable
    data class Data(
        val type: UtilityType,
        val content: ByteArray
    ) : Message()
}
