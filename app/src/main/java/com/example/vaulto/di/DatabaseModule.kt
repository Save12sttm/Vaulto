package com.example.vaulto.di

import android.content.Context
import com.example.vaulto.data.local.dao.VaultItemDao
import com.example.vaulto.data.local.database.VaultoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideVaultoDatabase(
        @ApplicationContext context: Context
    ): VaultoDatabase {
        val passphrase = "temporary_demo_passphrase_2024".toByteArray()
        return VaultoDatabase.getInstance(context, passphrase).apply {
            openHelper.writableDatabase.enableWriteAheadLogging()
        }
    }
    
    @Provides
    @Singleton
    fun provideVaultItemDao(
        database: VaultoDatabase
    ): VaultItemDao {
        return database.vaultItemDao()
    }
}