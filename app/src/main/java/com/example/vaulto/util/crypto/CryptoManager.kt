package com.example.vaulto.util.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import java.security.SecureRandom

object CryptoManager {
    private const val KEYSTORE_ALIAS = "VaultoKeyAlias"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128
    
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 100000
    private const val PBKDF2_KEY_LENGTH = 256

    fun generateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }
        
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    fun encrypt(data: ByteArray, key: SecretKey): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data)
        
        return EncryptedData(encryptedBytes, iv)
    }

    fun decrypt(encryptedData: EncryptedData, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        return cipher.doFinal(encryptedData.data)
    }

    fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            PBKDF2_KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun verifyPassword(
        inputPassword: String,
        storedHash: ByteArray,
        salt: ByteArray
    ): Boolean {
        val inputHash = hashPassword(inputPassword, salt)
        return inputHash.contentEquals(storedHash)
    }

    fun generateSecureRandom(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}

data class EncryptedData(
    val data: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedData

        if (!data.contentEquals(other.data)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}