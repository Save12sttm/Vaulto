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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: AuthViewModel,
    onSetupComplete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onSetupComplete()
        }
    }

    LaunchedEffect(password) {
        viewModel.checkPasswordStrength(password)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Master Password") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Welcome to Vaulto",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Create a strong master password to secure your vault",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Master Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
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
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (password.isNotEmpty()) {
                PasswordStrengthIndicator(strength = state.passwordStrength)
            }

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility 
                                         else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" 
                                               else "Show password"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None 
                                      else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                            viewModel.setupMasterPassword(password, confirmPassword)
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.setupMasterPassword(password, confirmPassword)
                },
                enabled = !state.isLoading && password.isNotEmpty() && confirmPassword.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Master Password")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Password Tips:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• Use at least 12 characters",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Mix uppercase, lowercase, numbers, and symbols",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Avoid common words and personal information",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• This password cannot be recovered if lost!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val color = when (strength) {
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrength.FAIR -> Color(0xFFFF9800)
        PasswordStrength.GOOD -> Color(0xFFFFC107)
        PasswordStrength.STRONG -> Color(0xFF8BC34A)
        PasswordStrength.VERY_STRONG -> Color(0xFF4CAF50)
    }

    val progress = when (strength) {
        PasswordStrength.WEAK -> 0.2f
        PasswordStrength.FAIR -> 0.4f
        PasswordStrength.GOOD -> 0.6f
        PasswordStrength.STRONG -> 0.8f
        PasswordStrength.VERY_STRONG -> 1.0f
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
        )
        Text(
            text = "Strength: ${strength.name.replace("_", " ")}",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}