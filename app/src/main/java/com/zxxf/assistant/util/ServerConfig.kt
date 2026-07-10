package com.zxxf.assistant.util

import android.content.Context

class ServerConfig(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
        set(value) {
            prefs.edit().putString(KEY_SERVER_URL, value).apply()
        }

    val isDefault: Boolean get() = !prefs.contains(KEY_SERVER_URL)

    companion object {
        private const val PREFS_NAME = "server_config"
        private const val KEY_SERVER_URL = "server_url"
        const val DEFAULT_URL = "http://8.130.212.251"
    }
}
