package com.example.vaulto.util.crypto

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TOTPGenerator {
    
    private const val TIME_STEP_SECONDS = 30
    private const val CODE_DIGITS = 6
    private const val ALGORITHM = "HmacSHA1"
    
    fun generateTOTP(secret: String, timeStepSeconds: Int = TIME_STEP_SECONDS): String {
        val decodedKey = Base32().decode(secret.replace(" ", "").uppercase())
        val time = System.currentTimeMillis() / 1000L / timeStepSeconds
        
        return generateCode(decodedKey, time, CODE_DIGITS)
    }
    
    fun getRemainingSeconds(timeStepSeconds: Int = TIME_STEP_SECONDS): Int {
        val currentTime = System.currentTimeMillis() / 1000L
        val elapsed = currentTime % timeStepSeconds
        return (timeStepSeconds - elapsed).toInt()
    }
    
    fun getProgress(timeStepSeconds: Int = TIME_STEP_SECONDS): Float {
        val remaining = getRemainingSeconds(timeStepSeconds)
        return remaining.toFloat() / timeStepSeconds
    }
    
    private fun generateCode(key: ByteArray, time: Long, digits: Int): String {
        val msg = ByteBuffer.allocate(8).putLong(time).array()
        val hash = hmacSha1(key, msg)
        val offset = (hash.last().toInt() and 0xF)
        
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
        
        val otp = binary % 10.0.pow(digits).toInt()
        return otp.toString().padStart(digits, '0')
    }
    
    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(SecretKeySpec(key, ALGORITHM))
        return mac.doFinal(data)
    }
    
    fun validateSecret(secret: String): Boolean {
        return try {
            val cleaned = secret.replace(" ", "").uppercase()
            Base32().decode(cleaned)
            true
        } catch (e: Exception) {
            false
        }
    }
}