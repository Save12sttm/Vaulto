package com.example.vaulto.ui.screens.vault

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vaulto.data.local.entities.VaultItemEntity

@Composable
fun CategoryChipsRow(
    items: List<VaultItemEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = remember(items) {
        listOf("All") + items.map { it.category }.distinct().sorted()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val count = if (category == "All") {
                items.size
            } else {
                items.count { it.category == category }
            }
            
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text("$category ($count)") },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortBottomSheet(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SortOrder.values().forEach { sortOrder ->
                ListItem(
                    headlineContent = { Text(sortOrder.displayName) },
                    leadingContent = {
                        RadioButton(
                            selected = currentSort == sortOrder,
                            onClick = null
                        )
                    },
                    modifier = Modifier.clickable {
                        onSortSelected(sortOrder)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyCategory(
    category: String,
    onResetFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No passwords in $category",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try selecting a different category",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onResetFilter) {
            Text("Show All")
        }
    }
}

enum class SortOrder(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_MODIFIED("Recently Modified"),
    DATE_CREATED("Recently Created"),
    CATEGORY("Category")
}