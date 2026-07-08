package com.zxxf.assistant.util

object FileUtil {

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            else -> String.format("%.1fMB", bytes / (1024.0 * 1024.0))
        }
    }

    fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        // Simple truncation to date-only format
        return isoDate.take(10)
    }
}
