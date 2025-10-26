package com.example.vaulto.di

import com.example.vaulto.data.repository.VaultRepositoryImpl
import com.example.vaulto.domain.repository.VaultRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindVaultRepository(
        vaultRepositoryImpl: VaultRepositoryImpl
    ): VaultRepository
}