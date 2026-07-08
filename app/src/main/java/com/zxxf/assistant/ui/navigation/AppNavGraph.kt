package com.zxxf.assistant.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zxxf.assistant.AppContainer
import com.zxxf.assistant.ui.auth.AuthViewModel
import com.zxxf.assistant.ui.auth.LoginScreen
import com.zxxf.assistant.ui.auth.ProfileEditScreen
import com.zxxf.assistant.ui.auth.RegisterScreen
import com.zxxf.assistant.ui.chat.ChatScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CHAT = "chat"
    const val PROFILE = "profile"
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
    var serverUrl by remember { mutableStateOf(appContainer.serverConfig.serverUrl) }

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
                serverUrl = serverUrl,
                onLogin = { username, password ->
                    appContainer.baseUrl = serverUrl
                    authViewModel.login(username, password)
                },
                onNavigateToRegister = {
                    authViewModel.clearError()
                    navController.navigate(Routes.REGISTER)
                },
                onClearError = { authViewModel.clearError() },
                onUpdateServerUrl = {
                    serverUrl = it
                    appContainer.baseUrl = it  // persist immediately
                }
            )

            // Navigate to chat when logged in (must be in LaunchedEffect, not composition)
            LaunchedEffect(authState.isLoggedIn) {
                if (authState.isLoggedIn) {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                uiState = authState,
                onRegister = { username, email, password, grade, major ->
                    appContainer.baseUrl = serverUrl
                    authViewModel.register(username, email, password, grade, major)
                },
                onNavigateBack = {
                    authViewModel.clearError()
                    navController.popBackStack()
                }
            )

            // Navigate to chat when registered (must be in LaunchedEffect, not composition)
            LaunchedEffect(authState.isLoggedIn) {
                if (authState.isLoggedIn) {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.PROFILE) {
            ProfileEditScreen(
                authRepository = appContainer.authRepository,
                onNavigateBack = { navController.popBackStack() },
                onProfileUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CHAT) {
            ChatScreen(
                appContainer = appContainer,
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
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
