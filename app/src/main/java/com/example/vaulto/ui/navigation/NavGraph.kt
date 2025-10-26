package com.example.vaulto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vaulto.ui.screens.auth.LockScreen
import com.example.vaulto.ui.screens.auth.SetupScreen
import com.example.vaulto.ui.screens.auth.AuthViewModel
import com.example.vaulto.ui.screens.vault.VaultListScreen
import com.example.vaulto.ui.screens.vault.VaultDetailScreen
import com.example.vaulto.ui.screens.vault.AddEditScreen
import com.example.vaulto.ui.screens.settings.SettingsScreen
import com.example.vaulto.ui.screens.health.PasswordHealthScreen
import com.example.vaulto.ui.screens.totp.TOTPScreen
import com.example.vaulto.ui.screens.backup.BackupScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = NavRoutes.Setup.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            SetupScreen(
                viewModel = viewModel,
                onSetupComplete = {
                    navController.navigate(NavRoutes.VaultList.route) {
                        popUpTo(NavRoutes.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = NavRoutes.Lock.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            LockScreen(
                viewModel = viewModel,
                onUnlockSuccess = {
                    navController.navigate(NavRoutes.VaultList.route) {
                        popUpTo(NavRoutes.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = NavRoutes.VaultList.route) {
            VaultListScreen(
                onItemClick = { itemId ->
                    navController.navigate(NavRoutes.VaultDetail.createRoute(itemId))
                },
                onAddClick = {
                    navController.navigate(NavRoutes.AddEdit.createRoute())
                },
                onPasswordHealthClick = {
                    navController.navigate(NavRoutes.PasswordHealth.route)
                },
                onTOTPClick = {
                    navController.navigate(NavRoutes.TOTP.route)
                },
                onSettingsClick = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onLockClick = {
                    navController.navigate(NavRoutes.Lock.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = NavRoutes.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = NavRoutes.PasswordHealth.route) {
            PasswordHealthScreen(
                onNavigateBack = { navController.navigateUp() },
                onItemClick = { itemId ->
                    navController.navigate(NavRoutes.VaultDetail.createRoute(itemId))
                }
            )
        }

        composable(route = NavRoutes.TOTP.route) {
            TOTPScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = NavRoutes.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = NavRoutes.VaultDetail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            VaultDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.navigateUp() },
                onEditClick = { 
                    navController.navigate(NavRoutes.AddEdit.createRoute(itemId))
                }
            )
        }

        composable(
            route = NavRoutes.AddEdit.route,
            arguments = listOf(
                navArgument("itemId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId")?.takeIf { it != -1L }
            AddEditScreen(
                itemId = itemId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}