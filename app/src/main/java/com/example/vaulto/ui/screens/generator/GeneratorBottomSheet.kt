package com.example.vaulto.ui.screens.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorBottomSheet(
    onDismiss: () -> Unit,
    onPasswordSelect: (String) -> Unit,
    viewModel: GeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password Generator",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = state.generatedPassword,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PasswordStrengthBar(
                        strength = state.passwordStrength,
                        entropy = state.entropy
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.generatePassword() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Regenerate")
                        }

                        Button(
                            onClick = {
                                copyToClipboard(context, "Password", state.generatedPassword)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }

                        Button(
                            onClick = {
                                onPasswordSelect(state.generatedPassword)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Length: ${state.length}",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = state.length.toFloat(),
                onValueChange = { viewModel.updateLength(it.toInt()) },
                valueRange = 4f..128f,
                steps = 123,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Character Types",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            CharacterTypeToggle(
                label = "Uppercase (A-Z)",
                checked = state.useUppercase,
                onCheckedChange = { viewModel.toggleUppercase() }
            )

            CharacterTypeToggle(
                label = "Lowercase (a-z)",
                checked = state.useLowercase,
                onCheckedChange = { viewModel.toggleLowercase() }
            )

            CharacterTypeToggle(
                label = "Numbers (0-9)",
                checked = state.useNumbers,
                onCheckedChange = { viewModel.toggleNumbers() }
            )

            CharacterTypeToggle(
                label = "Symbols (!@#$%)",
                checked = state.useSymbols,
                onCheckedChange = { viewModel.toggleSymbols() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CharacterTypeToggle(
                label = "Exclude Ambiguous (il1Lo0O)",
                checked = state.excludeAmbiguous,
                onCheckedChange = { viewModel.toggleExcludeAmbiguous() }
            )

            if (state.generationHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Recent Passwords",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                state.generationHistory.take(5).forEach { password ->
                    HistoryItem(
                        password = password,
                        onClick = {
                            viewModel.restoreFromHistory(password)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthBar(
    strength: PasswordStrength,
    entropy: Double
) {
    val color = when (strength) {
        PasswordStrength.VERY_WEAK -> Color(0xFFE53935)
        PasswordStrength.WEAK -> Color(0xFFFF6F00)
        PasswordStrength.FAIR -> Color(0xFFFBC02D)
        PasswordStrength.GOOD -> Color(0xFF7CB342)
        PasswordStrength.STRONG -> Color(0xFF43A047)
        PasswordStrength.VERY_STRONG -> Color(0xFF00897B)
    }

    val progress = when (strength) {
        PasswordStrength.VERY_WEAK -> 0.15f
        PasswordStrength.WEAK -> 0.3f
        PasswordStrength.FAIR -> 0.5f
        PasswordStrength.GOOD -> 0.7f
        PasswordStrength.STRONG -> 0.85f
        PasswordStrength.VERY_STRONG -> 1.0f
    }

    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strength.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
            Text(
                text = "${entropy.toInt()} bits",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CharacterTypeToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun HistoryItem(
    password: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = password,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}