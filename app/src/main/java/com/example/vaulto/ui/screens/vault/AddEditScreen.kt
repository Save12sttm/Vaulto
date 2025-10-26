package com.example.vaulto.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vaulto.ui.screens.generator.GeneratorBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    itemId: Long?,
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.addEditState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        if (itemId != null) {
            viewModel.loadItemForEdit(itemId)
        } else {
            viewModel.clearAddEditState()
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId != null) "Edit Password" else "Add Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveItem() },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title *") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username / Email") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            var showGeneratorSheet by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                             else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle visibility"
                            )
                        }
                        IconButton(onClick = { showGeneratorSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generate password",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                      else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (showGeneratorSheet) {
                GeneratorBottomSheet(
                    onDismiss = { showGeneratorSheet = false },
                    onPasswordSelect = { password ->
                        viewModel.updatePassword(password)
                    }
                )
            }

            OutlinedTextField(
                value = state.url,
                onValueChange = { viewModel.updateUrl(it) },
                label = { Text("Website URL") },
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            var expandedCategory by remember { mutableStateOf(false) }
            val categories = listOf("General", "Banking", "Social Media", "Shopping", "Email", "Work", "Entertainment", "Other")
            
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = state.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                viewModel.updateCategory(category)
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (state.isFavorite) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Favorite")
                }
                Switch(
                    checked = state.isFavorite,
                    onCheckedChange = { viewModel.toggleFavorite() }
                )
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.saveItem() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (itemId != null) "Update Password" else "Save Password")
            }
        }
    }
}