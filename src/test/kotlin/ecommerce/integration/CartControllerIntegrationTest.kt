package ecommerce.integration

import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.TokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class CartControllerIntegrationTest {
    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setup() {
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
    fun `should return 401 when no token provided`() {
        mockMvc.get("/api/cart-items").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 401 when invalid token provided`() {
        mockMvc.get("/api/cart-items") {
            header("Authorization", "Bearer invalid-token")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 200 and empty cart for authenticated user`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.get("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun `should add product to cart with valid token and product`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 2}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.memberId") { value(1) }
            jsonPath("$.productId") { value(1) }
            jsonPath("$.quantity") { value(2) }
        }
    }

    @Test
    fun `should return 404 when adding non-existent product to cart`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 999, "quantity": 1}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 400 for invalid add to cart request`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 0}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should update quantity of existing cart item`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 1}"""
        }

        mockMvc.put("/api/cart-items/1") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"quantity": 5}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.quantity") { value(5) }
        }
    }

    @Test
    fun `should return 404 when updating non-existent product`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.put("/api/cart-items/999") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"quantity": 1}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 400 when updating with invalid quantity`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 1}"""
        }

        mockMvc.put("/api/cart-items/1") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"quantity": 0}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should remove item from cart`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 1}"""
        }

        mockMvc.delete("/api/cart-items/1") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `should clear entire cart`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 1}"""
        }

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 2, "quantity": 2}"""
        }

        mockMvc.delete("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun `should handle duplicate product additions by updating quantity`() {
        val testMember = Member(1L, "test@email.com", "password", "Test User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 2}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.quantity") { value(2) }
        }

        mockMvc.post("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
            contentType = APPLICATION_JSON
            content = """{"productId": 1, "quantity": 3}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.quantity") { value(5) }
        }
    }
}
