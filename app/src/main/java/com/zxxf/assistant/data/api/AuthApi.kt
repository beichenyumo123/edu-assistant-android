package com.zxxf.assistant.data.api

import com.zxxf.assistant.data.dto.*
import retrofit2.http.*

interface AuthApi {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @GET("/api/auth/me")
    suspend fun getCurrentUser(): UserResponse

    @PUT("/api/auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UpdateProfileResponse
}
