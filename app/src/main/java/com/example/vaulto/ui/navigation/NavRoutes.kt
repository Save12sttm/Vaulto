package com.example.vaulto.ui.navigation

sealed class NavRoutes(val route: String) {
    object Setup : NavRoutes("setup")
    object Lock : NavRoutes("lock")
    object VaultList : NavRoutes("vault_list")
    object VaultDetail : NavRoutes("vault_detail/{itemId}") {
        fun createRoute(itemId: Long) = "vault_detail/$itemId"
    }
    object AddEdit : NavRoutes("add_edit?itemId={itemId}") {
        fun createRoute(itemId: Long? = null) = 
            if (itemId != null) "add_edit?itemId=$itemId" else "add_edit"
    }
    object Settings : NavRoutes("settings")
    object PasswordHealth : NavRoutes("password_health")
    object TOTP : NavRoutes("totp")
    object Backup : NavRoutes("backup")
}