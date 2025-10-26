package com.example.vaulto.ui.screens.vault

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vaulto.data.local.entities.VaultItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onItemClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onPasswordHealthClick: () -> Unit,
    onTOTPClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLockClick: () -> Unit
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_MODIFIED) }

    // Filter items based on category and sort
    val filteredItems = remember(state.items, selectedCategory, sortOrder) {
        val filtered = if (selectedCategory == "All") {
            state.items
        } else {
            state.items.filter { it.category == selectedCategory }
        }
        
        when (sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOrder.DATE_CREATED -> filtered.sortedByDescending { it.createdAt }
            SortOrder.DATE_MODIFIED -> filtered.sortedByDescending { it.modifiedAt }
            SortOrder.CATEGORY -> filtered.sortedBy { it.category }
        }
    }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { 
                        searchQuery = it
                        viewModel.searchItems(it)
                    },
                    onClose = {
                        showSearchBar = false
                        searchQuery = ""
                        viewModel.loadAllItems()
                    }
                )
            } else {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Vaulto")
                            if (filteredItems.isNotEmpty()) {
                                Text(
                                    text = "${filteredItems.size} password${if (filteredItems.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter & Sort")
                        }
                        IconButton(onClick = onPasswordHealthClick) {
                            Icon(Icons.Default.Shield, contentDescription = "Password Health")
                        }
                        IconButton(onClick = onTOTPClick) {
                            Icon(Icons.Default.Security, contentDescription = "Authenticator")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = onLockClick) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Password") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category chips
            CategoryChipsRow(
                items = state.items,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                filteredItems.isEmpty() && selectedCategory != "All" -> {
                    EmptyCategory(
                        category = selectedCategory,
                        onResetFilter = { selectedCategory = "All" }
                    )
                }
                filteredItems.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        onAddClick = onAddClick
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredItems,
                            key = { it.id }
                        ) { item ->
                            VaultItemCard(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                        
                        // Bottom padding for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Filter & Sort Bottom Sheet
    if (showFilterSheet) {
        FilterSortBottomSheet(
            currentSort = sortOrder,
            onSortSelected = { sortOrder = it },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search passwords...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close search")
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }
    )
}

@Composable
fun VaultItemCard(
    item: VaultItemEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.username.ifEmpty { "No username" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.url.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Show category badge
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LockOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No passwords yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first password to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Password")
        }
    }
}