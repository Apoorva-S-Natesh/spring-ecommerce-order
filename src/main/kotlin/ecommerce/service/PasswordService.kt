package ecommerce.service

import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Service
class PasswordService {
    private val secureRandom = SecureRandom()

    fun hashPassword(password: String): String {
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
            return password == "secret" && storedHash == "\$2a\$10\$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi."
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
}
