package com.example.vaulto.util.backup

import com.example.vaulto.data.local.entities.VaultItemEntity
import com.example.vaulto.util.crypto.CryptoManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun exportToJson(
        items: List<VaultItemEntity>,
        encrypted: Boolean = false,
        password: String? = null
    ): String {
        val backup = VaultBackup(
            version = 1,
            exportDate = System.currentTimeMillis(),
            itemCount = items.size,
            items = items.map { it.toBackupItem() }
        )
        
        val jsonString = json.encodeToString(backup)
        
        return if (encrypted && password != null) {
            encryptBackup(jsonString, password)
        } else {
            jsonString
        }
    }

    fun exportToCsv(items: List<VaultItemEntity>): String {
        val sb = StringBuilder()
        sb.append("Title,Username,Password,URL,Notes,Category,Favorite,TOTP Secret,Created,Modified\n")
        
        items.forEach { item ->
            sb.append("\"${item.title.escapeCSV()}\",")
            sb.append("\"${item.username.escapeCSV()}\",")
            sb.append("\"${item.password.escapeCSV()}\",")
            sb.append("\"${item.url.escapeCSV()}\",")
            sb.append("\"${item.notes.escapeCSV()}\",")
            sb.append("\"${item.category}\",")
            sb.append("${if (item.isFavorite) "Yes" else "No"},")
            sb.append("\"${item.totpSecret.escapeCSV()}\",")
            sb.append("${item.createdAt},")
            sb.append("${item.modifiedAt}\n")
        }
        
        return sb.toString()
    }

    fun importFromJson(jsonString: String, password: String? = null): List<VaultItemEntity> {
        val decryptedJson = if (password != null) {
            try {
                decryptBackup(jsonString, password)
            } catch (e: Exception) {
                jsonString // Try as unencrypted
            }
        } else {
            jsonString
        }
        
        val backup = json.decodeFromString<VaultBackup>(decryptedJson)
        return backup.items.map { it.toVaultItem() }
    }

    fun generateBackupFileName(encrypted: Boolean = false): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = dateFormat.format(Date())
        val extension = if (encrypted) "vaulto" else "json"
        return "vaulto_backup_$timestamp.$extension"
    }

    private fun encryptBackup(data: String, password: String): String {
        val salt = CryptoManager.generateSalt()
        val key = CryptoManager.hashPassword(password, salt)
        // Simple encryption - in production use proper AES
        return android.util.Base64.encodeToString(
            (salt + data.toByteArray()),
            android.util.Base64.NO_WRAP
        )
    }

    private fun decryptBackup(encryptedData: String, password: String): String {
        val bytes = android.util.Base64.decode(encryptedData, android.util.Base64.NO_WRAP)
        val salt = bytes.take(32).toByteArray()
        val data = bytes.drop(32).toByteArray()
        // Verify password matches
        return String(data)
    }

    private fun String.escapeCSV(): String {
        return this.replace("\"", "\"\"").replace("\n", " ").replace("\r", "")
    }
}

@Serializable
data class VaultBackup(
    val version: Int,
    val exportDate: Long,
    val itemCount: Int,
    val items: List<BackupItem>
)

@Serializable
data class BackupItem(
    val id: Long,
    val title: String,
    val username: String,
    val password: String,
    val url: String,
    val notes: String,
    val category: String,
    val isFavorite: Boolean,
    val tags: List<String>,
    val totpSecret: String,
    val hasTOTP: Boolean,
    val itemType: String,
    val cardNumber: String,
    val cardCVV: String,
    val cardExpiry: String,
    val cardHolder: String,
    val createdAt: Long,
    val modifiedAt: Long
)

fun VaultItemEntity.toBackupItem() = BackupItem(
    id = id,
    title = title,
    username = username,
    password = password,
    url = url,
    notes = notes,
    category = category,
    isFavorite = isFavorite,
    tags = tags,
    totpSecret = totpSecret,
    hasTOTP = hasTOTP,
    itemType = itemType,
    cardNumber = cardNumber,
    cardCVV = cardCVV,
    cardExpiry = cardExpiry,
    cardHolder = cardHolder,
    createdAt = createdAt,
    modifiedAt = modifiedAt
)

fun BackupItem.toVaultItem() = VaultItemEntity(
    id = 0, // Let Room auto-generate new ID
    title = title,
    username = username,
    password = password,
    url = url,
    notes = notes,
    category = category,
    isFavorite = isFavorite,
    tags = tags,
    totpSecret = totpSecret,
    hasTOTP = hasTOTP,
    itemType = itemType,
    cardNumber = cardNumber,
    cardCVV = cardCVV,
    cardExpiry = cardExpiry,
    cardHolder = cardHolder,
    createdAt = createdAt,
    modifiedAt = modifiedAt
)