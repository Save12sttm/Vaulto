package com.example.vaulto.data.local.dao

import androidx.room.*
import com.example.vaulto.data.local.entities.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultItemDao {
    
    @Query("SELECT * FROM vault_items ORDER BY modifiedAt DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>
    
    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemById(id: Long): VaultItemEntity?
    
    @Query("SELECT * FROM vault_items WHERE category = :category ORDER BY modifiedAt DESC")
    fun getItemsByCategory(category: String): Flow<List<VaultItemEntity>>
    
    @Query("SELECT * FROM vault_items WHERE isFavorite = 1 ORDER BY modifiedAt DESC")
    fun getFavoriteItems(): Flow<List<VaultItemEntity>>
    
    @Query("""
        SELECT * FROM vault_items 
        WHERE title LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        OR url LIKE '%' || :query || '%'
        OR notes LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE :query || '%' THEN 1
                WHEN username LIKE :query || '%' THEN 2
                ELSE 3
            END,
            modifiedAt DESC
    """)
    fun searchItems(query: String): Flow<List<VaultItemEntity>>
    
    @Query("SELECT DISTINCT category FROM vault_items ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT COUNT(*) FROM vault_items WHERE category = :category")
    suspend fun getItemCountByCategory(category: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity): Long
    
    @Update
    suspend fun updateItem(item: VaultItemEntity)
    
    @Delete
    suspend fun deleteItem(item: VaultItemEntity)
    
    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
    
    @Query("SELECT COUNT(*) FROM vault_items")
    suspend fun getItemCount(): Int
    
    @Query("UPDATE vault_items SET lastAccessedAt = :timestamp WHERE id = :id")
    suspend fun updateLastAccessed(id: Long, timestamp: Long)
}