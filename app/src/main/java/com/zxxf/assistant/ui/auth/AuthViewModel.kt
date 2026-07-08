package com.zxxf.assistant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zxxf.assistant.data.dto.UserDto
import com.zxxf.assistant.data.repository.AuthRepository
import com.zxxf.assistant.util.ErrorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: UserDto? = null,
    val error: String? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        if (authRepository.isLoggedIn()) {
            fetchUser()
        }
    }

    fun login(username: String, password: String) {
        // Client-side validation
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请填写用户名和密码")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = authRepository.login(username, password)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    user = response.user
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = ErrorParser.parse(e)
                )
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        grade: String?,
        major: String?
    ) {
        // Client-side validation matching backend requirements
        if (username.length < 3) {
            _uiState.value = _uiState.value.copy(error = "用户名至少3个字符")
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = _uiState.value.copy(error = "请输入有效的邮箱地址")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "密码至少6位")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = authRepository.register(username, email, password, grade, major)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    user = response.user
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = ErrorParser.parse(e)
                )
            }
        }
    }

    fun fetchUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = authRepository.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    user = response.user
                )
            } catch (e: Exception) {
                authRepository.logout()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    error = "认证已过期，请重新登录"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
