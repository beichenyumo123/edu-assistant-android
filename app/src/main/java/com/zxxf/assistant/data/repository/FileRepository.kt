package com.zxxf.assistant.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.zxxf.assistant.data.api.FileApi
import com.zxxf.assistant.data.dto.FileListResponse
import com.zxxf.assistant.data.dto.FileUploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class FileRepository(private val fileApiProvider: () -> FileApi) {

    private val fileApi: FileApi get() = fileApiProvider()

    suspend fun list(): FileListResponse {
        return fileApi.list()
    }

    suspend fun upload(context: Context, uri: Uri): FileUploadResponse {
        val file = copyUriToTempFile(context, uri)
        val fileName = getFileName(context, uri) ?: "upload"

        val requestBody = file.asRequestBody(
            fileName.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()!!
        )
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

        val response = fileApi.upload(part)

        // Clean up temp file
        file.delete()

        return response
    }

    suspend fun delete(fileId: Long) {
        fileApi.delete(fileId)
    }

    private fun copyUriToTempFile(context: Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
