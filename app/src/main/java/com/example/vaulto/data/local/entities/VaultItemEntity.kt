package com.example.vaulto.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.vaulto.data.local.database.Converters

@Entity(tableName = "vault_items")
@TypeConverters(Converters::class)
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val url: String = "",
    val notes: String = "",
    val category: String = "General",
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val customFields: Map<String, String> = emptyMap(),
    val totpSecret: String = "",
    val hasTOTP: Boolean = false,
    val itemType: String = "password", // password, card, identity, note
    val cardNumber: String = "",
    val cardCVV: String = "",
    val cardExpiry: String = "",
    val cardHolder: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = 0
)