package com.example.vaulto.ui.screens.totp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vaulto.util.crypto.TOTPGenerator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TOTPScreen(
    viewModel: TOTPViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTOTPItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authenticator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Code") }
            )
        }
    ) { paddingValues ->
        if (state.items.isEmpty()) {
            EmptyTOTPState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onAddClick = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items) { item ->
                    TOTPCard(
                        item = item,
                        currentTime = currentTime
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Add TOTP Dialog
        if (showAddDialog) {
            AddTOTPDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, username, secret ->
                    viewModel.addTOTPCode(title, username, secret)
                }
            )
        }
    }
}

@Composable
fun TOTPCard(
    item: com.example.vaulto.data.local.entities.VaultItemEntity,
    currentTime: Long
) {
    val context = LocalContext.current
    val code = remember(currentTime / 30000) {
        TOTPGenerator.generateTOTP(item.totpSecret)
    }
    
    val remaining = remember(currentTime) {
        TOTPGenerator.getRemainingSeconds()
    }
    
    val progress = remember(currentTime) {
        TOTPGenerator.getProgress()
    }
    
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (item.username.isNotEmpty()) {
                        Text(
                            text = item.username,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "${remaining}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (remaining <= 5) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = code.chunked(3).joinToString(" "),
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = { 
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("TOTP Code", code)
                    clipboard.setPrimaryClip(clip)
                }) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (remaining <= 5) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun EmptyTOTPState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No authenticator codes yet",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add 2FA codes to secure your accounts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Code")
        }
    }
}