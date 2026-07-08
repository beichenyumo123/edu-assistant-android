package com.zxxf.assistant.ui.chat.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zxxf.assistant.data.dto.MemoryResponse
import com.zxxf.assistant.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryUiState(
    val memory: MemoryResponse? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isClearing: Boolean = false,
    val error: String? = null,
    val clearSuccess: Boolean = false
)

class MemoryViewModel(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init {
        loadMemory()
    }

    fun loadMemory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = memoryRepository.getMemory()
                _uiState.update { it.copy(memory = response, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "加载记忆失败: ${e.message}", isLoading = false) }
            }
        }
    }

    fun saveSettings(
        memoryEnabled: Boolean? = null,
        preferredAnswerStyle: String? = null,
        communicationTone: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val response = memoryRepository.updateMemory(memoryEnabled, preferredAnswerStyle, communicationTone)
                _uiState.update { it.copy(memory = response, isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存失败: ${e.message}", isSaving = false) }
            }
        }
    }

    fun clearMemory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearing = true, error = null) }
            try {
                memoryRepository.clearMemory()
                _uiState.update { it.copy(isClearing = false, clearSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "清空失败: ${e.message}", isClearing = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, clearSuccess = false) }
    }

    class Factory(
        private val memoryRepository: MemoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemoryViewModel(memoryRepository) as T
        }
    }
}
