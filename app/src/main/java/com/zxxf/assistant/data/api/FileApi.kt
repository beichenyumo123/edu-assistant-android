package com.zxxf.assistant.data.api

import com.zxxf.assistant.data.dto.DeleteFileResponse
import com.zxxf.assistant.data.dto.FileListResponse
import com.zxxf.assistant.data.dto.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface FileApi {

    @Multipart
    @POST("/api/files/upload")
    suspend fun upload(@Part file: MultipartBody.Part): FileUploadResponse

    @GET("/api/files")
    suspend fun list(): FileListResponse

    @DELETE("/api/files/{id}")
    suspend fun delete(@Path("id") fileId: Long): DeleteFileResponse
}
