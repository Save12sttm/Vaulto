package com.example.vaulto.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vaulto.util.biometric.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    viewModel: AuthViewModel,
    onUnlockSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val isBiometricAvailable = remember {
        BiometricHelper.isBiometricAvailable(context)
    }
    
    val biometricPrompt = remember {
        if (context is FragmentActivity && isBiometricAvailable) {
            BiometricHelper.createBiometricPrompt(
                activity = context,
                onSuccess = {
                    viewModel.authenticateWithBiometric()
                },
                onError = { error ->
                    viewModel.setError(error)
                }
            )
        } else null
    }

    // Navigate on success
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onUnlockSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unlock Vaulto") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Enter Master Password",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Unlock your vault to access your passwords",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    viewModel.clearError()
                },
                label = { Text("Master Password") },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility 
                                         else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" 
                                               else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None 
                                      else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        if (password.isNotEmpty()) {
                            viewModel.verifyMasterPassword(password)
                        }
                    }
                ),
                isError = state.errorMessage != null,
                modifier = Modifier.fillMaxWidth()
            )

            // Error message
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Unlock button
            Button(
                onClick = {
                    viewModel.verifyMasterPassword(password)
                },
                enabled = !state.isLoading && password.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Unlock")
            }

            // Biometric button
            if (isBiometricAvailable && biometricPrompt != null) {
                OutlinedButton(
                    onClick = {
                        val promptInfo = BiometricHelper.createPromptInfo()
                        biometricPrompt.authenticate(promptInfo)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Biometric")
                }
            }
        }
    }
}