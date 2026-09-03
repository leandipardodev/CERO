package com.utilities.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class MicrophoneConfig(
    val sampleRate: Int = 44100,
    val channels: Int = 1,
    val bitsPerSample: Int = 16,
    val noiseReduction: Boolean = true,
    val gain: Float = 1.0f
)
