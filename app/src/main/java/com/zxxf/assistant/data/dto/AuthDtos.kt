package com.zxxf.assistant.data.dto

import com.google.gson.annotations.SerializedName

// ── Requests ──

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val grade: String? = null,   // 部门
    val major: String? = null    // 岗位
)

data class UpdateProfileRequest(
    val username: String? = null,
    val email: String? = null,
    val grade: String? = null,
    val major: String? = null
)

// ── Responses ──

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: UserDto
)

data class UserResponse(
    val user: UserDto
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val grade: String? = null,
    val major: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null
)

data class UpdateProfileResponse(
    val success: Boolean,
    val user: UserDto
)
