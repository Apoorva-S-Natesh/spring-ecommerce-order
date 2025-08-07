package ecommerce.service

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Service
class PasswordService {
    private val secureRandom = SecureRandom()

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    @Deprecated("Legacy method - kept for backward compatibility with existing SHA-256 hashes")
    fun hashPasswordLegacy(password: String): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        val saltedPassword = password + Base64.getEncoder().encodeToString(salt)
        val hashedBytes = MessageDigest.getInstance("SHA-256").digest(saltedPassword.toByteArray())
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashedBytes)
    }

    fun verifyPassword(
        password: String,
        storedHash: String,
    ): Boolean {
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return verifyBCryptPassword(password, storedHash)
        }

        if (!storedHash.contains(":")) {
            return password == storedHash
        }
        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val salt = parts[0]
        val expectedHash = parts[1]
        val saltedPassword = password + salt
        val hashedBytes = MessageDigest.getInstance("SHA-256").digest(saltedPassword.toByteArray())
        val actualHash = Base64.getEncoder().encodeToString(hashedBytes)

        return actualHash == expectedHash
    }

    private fun verifyBCryptPassword(
        password: String,
        storedHash: String,
    ): Boolean {
        return BCrypt.checkpw(password, storedHash)
    }
}
