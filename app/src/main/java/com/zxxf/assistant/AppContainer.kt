package com.zxxf.assistant

import android.content.Context
import com.zxxf.assistant.data.api.*
import com.zxxf.assistant.data.repository.*
import com.zxxf.assistant.util.AuthInterceptor
import com.zxxf.assistant.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    val tokenManager = TokenManager(context)

    // ── OkHttp ──

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenManager))
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)  // RAG answers can be slow
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Retrofit ──

    var baseUrl: String = DEFAULT_BASE_URL

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ── API interfaces ──

    val authApi: AuthApi get() = retrofit.create(AuthApi::class.java)
    val chatApi: ChatApi get() = retrofit.create(ChatApi::class.java)
    val conversationApi: ConversationApi get() = retrofit.create(ConversationApi::class.java)
    val fileApi: FileApi get() = retrofit.create(FileApi::class.java)
    val toolApi: ToolApi get() = retrofit.create(ToolApi::class.java)
    val memoryApi: MemoryApi get() = retrofit.create(MemoryApi::class.java)

    // ── Repositories ──

    val authRepository: AuthRepository get() = AuthRepository(authApi, tokenManager)
    val conversationRepository: ConversationRepository get() = ConversationRepository(conversationApi)
    val fileRepository: FileRepository get() = FileRepository(fileApi)
    val toolRepository: ToolRepository get() = ToolRepository(toolApi)
    val memoryRepository: MemoryRepository get() = MemoryRepository(memoryApi)
    val chatRepository: ChatRepository get() = ChatRepository(chatApi, baseUrl, tokenManager)

    companion object {
        // Android emulator -> host machine
        const val EMULATOR_BASE_URL = "http://10.0.2.2:8000"
        // Default: same as emulator (change for real device / production)
        const val DEFAULT_BASE_URL = EMULATOR_BASE_URL
    }
}
