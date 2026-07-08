package com.zxxf.assistant.data.dto

import com.google.gson.annotations.SerializedName

// ── List ──

data class FileListResponse(
    val files: List<DocumentDto>
)

// ── Upload ──

data class FileUploadResponse(
    val success: Boolean,
    val file: DocumentDto
)

data class DocumentDto(
    val id: Long,
    @SerializedName("user_id") val userId: Int,
    val filename: String,
    @SerializedName("original_name") val originalName: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("chunk_count") val chunkCount: Int? = null,
    val status: String,
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// ── Delete ──

data class DeleteFileResponse(
    val success: Boolean,
    val message: String
)
