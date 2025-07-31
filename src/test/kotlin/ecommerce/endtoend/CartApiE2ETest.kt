package ecommerce.endtoend

import ecommerce.model.Member
import ecommerce.service.TokenService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
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

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost:$port"
        RestAssured.basePath = "/api"
        jdbcTemplate.update("DELETE FROM cart")
        jdbcTemplate.update("DELETE FROM members")
        jdbcTemplate.update("DELETE FROM products")
        jdbcTemplate.update("ALTER TABLE cart ALTER COLUMN id RESTART WITH 1")
        jdbcTemplate.update("ALTER TABLE members ALTER COLUMN id RESTART WITH 1")
        jdbcTemplate.update("ALTER TABLE products ALTER COLUMN id RESTART WITH 1")
        jdbcTemplate.update(
            """
            INSERT INTO products (id, name, price, imageUrl) 
            VALUES (1, 'Test Product', 100.0, 'http://example.com/test.jpg')
        """,
        )

        jdbcTemplate.update(
            """
            INSERT INTO members (id, email, password, name, role, created_at)
            VALUES (1, 'test@email.com', 'password', 'Test User', 'USER', CURRENT_TIMESTAMP)
        """,
        )
    }

    @Test
    fun getCartItems_authenticatedUser_shouldReturnItems() {
        val testUser = Member(1L, "test@example.com", "password", "Test User")
        val token = tokenService.generateToken(testUser)

        try {
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .contentType(ContentType.JSON)
                .`when`()
                .get("/cart-items")
                .then()
                .statusCode(200)
        } catch (e: Exception) {
            println("Test failed with exception: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
