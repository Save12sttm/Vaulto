package com.example.vaulto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ItemTypeChip(
            label = "Password",
            icon = Icons.Default.Lock,
            selected = selectedType == "password",
            onClick = { onTypeSelected("password") },
            modifier = Modifier.weight(1f)
        )
        ItemTypeChip(
            label = "Card",
            icon = Icons.Default.CreditCard,
            selected = selectedType == "card",
            onClick = { onTypeSelected("card") },
            modifier = Modifier.weight(1f)
        )
        ItemTypeChip(
            label = "Note",
            icon = Icons.Default.Notes,
            selected = selectedType == "note",
            onClick = { onTypeSelected("note") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ItemTypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = modifier
    )
}