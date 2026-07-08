package com.zxxf.assistant.ui.knowledge

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zxxf.assistant.data.dto.DocumentDto
import com.zxxf.assistant.data.dto.ExtractKnowledgeResponse
import com.zxxf.assistant.data.dto.KnowledgePointDto
import com.zxxf.assistant.data.repository.FileRepository
import com.zxxf.assistant.data.repository.ToolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KnowledgeUiState(
    val documents: List<DocumentDto> = emptyList(),
    val isUploading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    // Summary
    val isSummarizing: Boolean = false,
    val summary: String? = null,
    val summaryDocumentName: String? = null,
    // Knowledge cards
    val isExtracting: Boolean = false,
    val knowledgePoints: List<KnowledgePointDto>? = null,
    val knowledgeDocumentName: String? = null
)

class KnowledgeViewModel(
    private val fileRepository: FileRepository,
    private val toolRepository: ToolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KnowledgeUiState())
    val uiState: StateFlow<KnowledgeUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            try {
                val response = fileRepository.list()
                _uiState.update { it.copy(documents = response.files) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "加载文档列表失败: ${e.message}") }
            }
        }
    }

    fun uploadFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            try {
                fileRepository.upload(context, uri)
                loadDocuments()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "上传失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun deleteFile(fileId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            try {
                fileRepository.delete(fileId)
                loadDocuments()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }

    fun summarize(documentId: Long, documentName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, summary = null, summaryDocumentName = documentName) }
            try {
                val response = toolRepository.summarize(documentId)
                _uiState.update { it.copy(summary = response.summary) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "摘要生成失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isSummarizing = false) }
            }
        }
    }

    fun extractKnowledge(documentId: Long, documentName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExtracting = true, knowledgePoints = null, knowledgeDocumentName = documentName) }
            try {
                val response = toolRepository.extractKnowledge(documentId)
                _uiState.update { it.copy(knowledgePoints = response.knowledgePoints) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "知识卡片提取失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isExtracting = false) }
            }
        }
    }

    fun clearSummary() {
        _uiState.update { it.copy(summary = null, summaryDocumentName = null) }
    }

    fun clearKnowledgeCards() {
        _uiState.update { it.copy(knowledgePoints = null, knowledgeDocumentName = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(
        private val fileRepository: FileRepository,
        private val toolRepository: ToolRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KnowledgeViewModel(fileRepository, toolRepository) as T
        }
    }
}
