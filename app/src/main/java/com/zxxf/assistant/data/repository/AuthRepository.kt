package com.zxxf.assistant.data.repository

import com.zxxf.assistant.data.api.AuthApi
import com.zxxf.assistant.data.dto.*
import com.zxxf.assistant.util.TokenManager

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): TokenResponse {
        val response = authApi.login(LoginRequest(username, password))
        tokenManager.token = response.accessToken
        return response
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        grade: String?,
        major: String?
    ): TokenResponse {
        val response = authApi.register(
            RegisterRequest(username, email, password, grade, major)
        )
        tokenManager.token = response.accessToken
        return response
    }

    suspend fun getCurrentUser(): UserResponse {
        return authApi.getCurrentUser()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): UpdateProfileResponse {
        return authApi.updateProfile(request)
    }

    fun logout() {
        tokenManager.clear()
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn
}
