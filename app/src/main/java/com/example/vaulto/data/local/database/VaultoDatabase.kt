package com.example.vaulto.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vaulto.data.local.dao.VaultItemDao
import com.example.vaulto.data.local.entities.VaultItemEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [VaultItemEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VaultoDatabase : RoomDatabase() {
    
    abstract fun vaultItemDao(): VaultItemDao
    
    companion object {
        private const val DATABASE_NAME = "vaulto_database.db"
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE vault_items ADD COLUMN totpSecret TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE vault_items ADD COLUMN hasTOTP INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE vault_items ADD COLUMN itemType TEXT NOT NULL DEFAULT 'password'")
                database.execSQL("ALTER TABLE vault_items ADD COLUMN cardNumber TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vault_items ADD COLUMN cardCVV TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vault_items ADD COLUMN cardExpiry TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vault_items ADD COLUMN cardHolder TEXT NOT NULL DEFAULT ''")
            }
        }
        
        @Volatile
        private var INSTANCE: VaultoDatabase? = null
        
        fun getInstance(context: Context, passphrase: ByteArray): VaultoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultoDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(SupportOpenHelperFactory(passphrase))
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}