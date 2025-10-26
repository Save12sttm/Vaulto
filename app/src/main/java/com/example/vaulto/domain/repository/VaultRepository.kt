package com.example.vaulto.domain.repository

import com.example.vaulto.data.local.entities.VaultItemEntity
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun getAllItems(): Flow<List<VaultItemEntity>>
    fun getItemById(id: Long): Flow<VaultItemEntity?>
    fun getFavoriteItems(): Flow<List<VaultItemEntity>>
    fun searchItems(query: String): Flow<List<VaultItemEntity>>
    suspend fun insertItem(item: VaultItemEntity): Long
    suspend fun updateItem(item: VaultItemEntity)
    suspend fun deleteItem(item: VaultItemEntity)
    suspend fun getItemCount(): Int
}