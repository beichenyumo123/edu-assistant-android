package com.zxxf.assistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zxxf.assistant.AppContainer
import com.zxxf.assistant.ui.auth.AuthViewModel
import com.zxxf.assistant.ui.auth.LoginScreen
import com.zxxf.assistant.ui.auth.RegisterScreen
import com.zxxf.assistant.ui.chat.ChatScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CHAT = "chat"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(appContainer.authRepository)
    )
    val authState by authViewModel.uiState.collectAsState()

    // Determine start destination based on login state
    val startDestination = if (appContainer.authRepository.isLoggedIn()) {
        Routes.CHAT
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                uiState = authState,
                onLogin = { username, password ->
                    authViewModel.login(username, password)
                },
                onNavigateToRegister = {
                    authViewModel.clearError()
                    navController.navigate(Routes.REGISTER)
                },
                onClearError = { authViewModel.clearError() }
            )

            // Navigate to chat when logged in
            if (authState.isLoggedIn) {
                navController.navigate(Routes.CHAT) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                uiState = authState,
                onRegister = { username, email, password, grade, major ->
                    authViewModel.register(username, email, password, grade, major)
                },
                onNavigateBack = {
                    authViewModel.clearError()
                    navController.popBackStack()
                }
            )

            // Navigate to chat when registered
            if (authState.isLoggedIn) {
                navController.navigate(Routes.CHAT) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Routes.CHAT) {
            ChatScreen(
                appContainer = appContainer,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
