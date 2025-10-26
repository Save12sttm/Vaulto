package com.example.vaulto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.vaulto.data.local.PreferencesManager
import com.example.vaulto.ui.navigation.NavGraph
import com.example.vaulto.ui.navigation.NavRoutes
import com.example.vaulto.ui.theme.VaultoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isFirstLaunch by preferencesManager.isFirstLaunch.collectAsState(initial = true)
            val systemDarkTheme = isSystemInDarkTheme()
            
            VaultoTheme(darkTheme = systemDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    val startDestination = if (isFirstLaunch) {
                        NavRoutes.Setup.route
                    } else {
                        NavRoutes.Lock.route
                    }
                    
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}