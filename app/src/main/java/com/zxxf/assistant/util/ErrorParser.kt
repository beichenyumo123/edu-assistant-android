package com.zxxf.assistant.util

import com.google.gson.Gson
import retrofit2.HttpException

data class FastApiError(
    val detail: Any? = null  // Can be String or List of error objects
)

data class ValidationError(
    val loc: List<String>? = null,
    val msg: String? = null,
    val type: String? = null
)

object ErrorParser {

    private val gson = Gson()

    fun parse(e: Exception): String {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            return try {
                // Try to parse as FastAPI validation error (list of errors)
                val validationErrors = gson.fromJson(
                    errorBody,
                    Array<ValidationError>::class.java
                )
                if (validationErrors != null && validationErrors.isNotEmpty()) {
                    validationErrors.joinToString("\n") { err ->
                        val field = err.loc?.lastOrNull() ?: ""
                        val msg = err.msg ?: ""
                        if (field.isNotEmpty()) "$field: $msg" else msg
                    }
                } else {
                    // Try single detail field
                    val fastApiError = gson.fromJson(errorBody, FastApiError::class.java)
                    when (val d = fastApiError.detail) {
                        is String -> d
                        is List<*> -> d.joinToString("\n") {
                            when (it) {
                                is Map<*, *> -> {
                                    val loc = (it["loc"] as? List<*>)?.lastOrNull() ?: ""
                                    val msg = it["msg"] ?: ""
                                    "$loc: $msg"
                                }
                                else -> it.toString()
                            }
                        }
                        else -> "服务器错误 (${e.code()})"
                    }
                }
            } catch (_: Exception) {
                "服务器错误 (${e.code()}): $errorBody"
            }
        }
        return e.message ?: "网络错误"
    }
}
