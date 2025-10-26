package com.example.vaulto.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun QuickActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCopyPassword: () -> Unit,
    onCopyUsername: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = 0.dp, y = 8.dp)
    ) {
        DropdownMenuItem(
            text = { Text("Copy Password") },
            onClick = {
                onCopyPassword()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Copy Username") },
            onClick = {
                onCopyUsername()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Edit") },
            onClick = {
                onEdit()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                onDelete()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null)
            },
            colors = MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.error
            )
        )
    }
}