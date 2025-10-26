package com.example.vaulto.ui.screens.totp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vaulto.util.crypto.TOTPGenerator

@Composable
fun AddTOTPDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Authenticator Code") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name (e.g. Google, GitHub)") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Account (optional)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = secret,
                    onValueChange = { 
                        secret = it
                        errorMessage = null
                    },
                    label = { Text("Secret Key") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    supportingText = { 
                        Text("Enter the secret key from the app/website")
                    },
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "How to add:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "1. Go to the app's 2FA setup",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "2. Choose 'Manual entry' or 'Can't scan QR?'",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "3. Copy the secret key and paste here",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        title.isEmpty() -> {
                            errorMessage = "Name is required"
                        }
                        secret.isEmpty() -> {
                            errorMessage = "Secret key is required"
                        }
                        !TOTPGenerator.validateSecret(secret) -> {
                            errorMessage = "Invalid secret key format"
                        }
                        else -> {
                            onAdd(title, username, secret)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}