package ecommerce.unit.service

import ecommerce.service.PasswordService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@DisplayName("PasswordService Core Tests")
class PasswordServiceTest {
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        passwordService = PasswordService()
    }

    @Test
    @DisplayName("should hash password with salt")
    fun shouldHashPasswordWithSalt() {
        val password = "testPassword123"
        val hash = passwordService.hashPassword(password)

        assertAll(
            { assertThat(hash).isNotNull() },
            { assertThat(hash).contains(":") },
            { assertThat(hash.split(":")).hasSize(2) },
        )
    }

    @Test
    @DisplayName("should verify correct password")
    fun shouldVerifyCorrectPassword() {
        val password = "myPassword123"
        val hash = passwordService.hashPassword(password)

        val result = passwordService.verifyPassword(password, hash)

        assertThat(result).isTrue()
    }

    @Test
    @DisplayName("should reject incorrect password")
    fun shouldRejectIncorrectPassword() {
        val correctPassword = "myPassword123"
        val wrongPassword = "wrongPassword"
        val hash = passwordService.hashPassword(correctPassword)

        val result = passwordService.verifyPassword(wrongPassword, hash)

        assertThat(result).isFalse()
    }

    @Test
    @DisplayName("should generate unique salts")
    fun shouldGenerateUniqueSalts() {
        val password = "samePassword"

        val hash1 = passwordService.hashPassword(password)
        val hash2 = passwordService.hashPassword(password)

        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    @DisplayName("should handle malformed hash")
    fun shouldHandleMalformedHash() {
        val password = "testPassword"
        val malformedHash = "invalidhash"

        val result = passwordService.verifyPassword(password, malformedHash)

        assertThat(result).isFalse()
    }
}
