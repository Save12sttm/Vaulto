package com.example.vaulto.ui.screens.vault

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDetailScreen(
    itemId: Long,
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.loadItemById(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog(true) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.item != null -> {
                val item = state.item!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailField(
                        label = "Title",
                        value = item.title,
                        icon = Icons.Default.Title
                    )

                    DetailField(
                        label = "Username",
                        value = item.username,
                        icon = Icons.Default.Person,
                        onCopy = {
                            copyToClipboard(context, "Username", item.username)
                        }
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Password",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility
                                                         else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle visibility"
                                        )
                                    }
                                    IconButton(onClick = {
                                        copyToClipboard(context, "Password", item.password)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy"
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (passwordVisible) item.password else "••••••••",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    if (item.url.isNotEmpty()) {
                        DetailField(
                            label = "Website",
                            value = item.url,
                            icon = Icons.Default.Language,
                            onCopy = {
                                copyToClipboard(context, "URL", item.url)
                            }
                        )
                    }

                    if (item.notes.isNotEmpty()) {
                        DetailField(
                            label = "Notes",
                            value = item.notes,
                            icon = Icons.Default.Notes
                        )
                    }

                    DetailField(
                        label = "Category",
                        value = item.category,
                        icon = Icons.Default.Category
                    )
                }
            }
        }

        if (state.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteDialog(false) },
                title = { Text("Delete Password") },
                text = { Text("Are you sure you want to delete '${state.item?.title}'? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            state.item?.let { viewModel.deleteItem(it) }
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteDialog(false) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCopy: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (onCopy != null) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}