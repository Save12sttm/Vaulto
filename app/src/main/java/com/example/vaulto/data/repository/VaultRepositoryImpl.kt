package com.example.vaulto.data.repository

import com.example.vaulto.data.local.dao.VaultItemDao
import com.example.vaulto.data.local.entities.VaultItemEntity
import com.example.vaulto.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VaultRepositoryImpl @Inject constructor(
    private val dao: VaultItemDao
) : VaultRepository {

    override fun getAllItems(): Flow<List<VaultItemEntity>> = dao.getAllItems()

    override fun getItemById(id: Long): Flow<VaultItemEntity?> = flow {
        emit(dao.getItemById(id))
    }

    override fun getFavoriteItems(): Flow<List<VaultItemEntity>> = dao.getFavoriteItems()

    override fun searchItems(query: String): Flow<List<VaultItemEntity>> = dao.searchItems(query)

    override suspend fun insertItem(item: VaultItemEntity): Long = dao.insertItem(item)

    override suspend fun updateItem(item: VaultItemEntity) = dao.updateItem(item)

    override suspend fun deleteItem(item: VaultItemEntity) = dao.deleteItem(item)

    override suspend fun getItemCount(): Int = dao.getItemCount()
}