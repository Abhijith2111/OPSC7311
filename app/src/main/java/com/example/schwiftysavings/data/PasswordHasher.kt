package com.example.schwiftysavings.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Simple salted SHA-256 hashing for local login.
 * Reference: Android security best practices for offline demos (not production KDF).
 */
object PasswordHasher {
    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun matches(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }
}