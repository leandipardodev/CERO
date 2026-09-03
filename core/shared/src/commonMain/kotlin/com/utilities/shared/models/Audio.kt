package com.utilities.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioConfig(
    val sampleRate: Int = 44100,
    val channels: Int = 1,
    val bitsPerSample: Int = 16,
    val bufferSize: Int = 4096
)

@Serializable
data class AudioChunk(
    val timestamp: Long,
    val data: ByteArray
)
