package com.zxxf.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.zxxf.assistant.ui.navigation.AppNavGraph
import com.zxxf.assistant.ui.theme.AssistantTheme

class MainActivity : ComponentActivity() {

    lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer(applicationContext)

        // Set base URL from shared prefs or intent (for production)
        // appContainer.baseUrl = "https://your-server.com"

        setContent {
            AssistantTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    appContainer = appContainer
                )
            }
        }
    }
}
