package com.utilities.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class FileMetadata(
    val name: String,
    val size: Long,
    val mimeType: String,
    val checksum: String
)

@Serializable
data class FileChunk(
    val fileId: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val data: ByteArray
)
