package com.example.data.security

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CoupleEncryption {
    private const val ALGORITHM = "AES"
    
    // Default 128-bit key for demonstration.
    // In a production app, this would be negotiated peer-to-peer (DHT or DH Key Exchange)
    private var sharedSecret: String = "HeartSyncLoveKey" 

    fun getSharedSecret(): String = sharedSecret

    fun updateSharedSecret(newSecret: String) {
        // Enforce exactly 16 bytes for AES-128
        val bytes = newSecret.toByteArray(Charsets.UTF_8)
        sharedSecret = if (bytes.size >= 16) {
            newSecret.substring(0, 16)
        } else {
            newSecret.padEnd(16, '♥')
        }
    }

    /**
     * Encrypts plain text into AES Base64 string
     */
    fun encrypt(data: String): String {
        if (data.isEmpty()) return ""
        return try {
            val keyBytes = sharedSecret.toByteArray(Charsets.UTF_8).copyOf(16)
            val keySpec = SecretKeySpec(keyBytes, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
        } catch (e: Exception) {
            "ErrorEncrypting:${e.localizedMessage}"
        }
    }

    /**
     * Decrypts AES Base64 Encrypted string to plain text
     */
    fun decrypt(encryptedData: String): String {
        if (encryptedData.isEmpty()) return ""
        if (!encryptedData.endsWith("=") && encryptedData.length < 10) return encryptedData // Raw text
        return try {
            val keyBytes = sharedSecret.toByteArray(Charsets.UTF_8).copyOf(16)
            val keySpec = SecretKeySpec(keyBytes, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Recovers gracefully: if key is different, show cipher text to represent secure encryption mismatch
            "[Encrypted Content: $encryptedData]"
        }
    }
}
