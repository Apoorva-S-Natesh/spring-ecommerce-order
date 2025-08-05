package ecommerce.endtoend

import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.TokenService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class CartApiE2ETest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var tokenService: TokenService

    private fun getBaseUrl() = "http://localhost:$port"

    @BeforeEach
    fun setup() {
    }

    @Test
    fun getCartItems_authenticatedUser_shouldReturnItems() {
        val testUser = Member(
            email = "test@example.com", 
            password = "password", 
            name = "Test User", 
            role = Role.USER,
            id = 1L
        )
        val token = tokenService.generateToken(testUser)

        try {
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .`when`()
                .get("${getBaseUrl()}/api/cart-items")
                .then()
                .statusCode(200)
        } catch (e: Exception) {
            println("Test failed with exception: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}