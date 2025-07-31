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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class AnalyticsControllerIntegrationTest {
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
            VALUES (1, 'Product A', 100.0, 'http://example.com/a.jpg')
        """,
        )
        jdbcTemplate.update(
            """
            INSERT INTO products (id, name, price, imageUrl) 
            VALUES (2, 'Product B', 200.0, 'http://example.com/b.jpg')
        """,
        )

        jdbcTemplate.update(
            """
            INSERT INTO members (id, email, password, name, role, created_at)
            VALUES (1, 'admin@email.com', 'password', 'Admin', 'ADMIN', CURRENT_TIMESTAMP)
        """,
        )
        jdbcTemplate.update(
            """
            INSERT INTO members (id, email, password, name, role, created_at)
            VALUES (2, 'user@email.com', 'password', 'User', 'USER', CURRENT_TIMESTAMP)
        """,
        )
    }

    @Test
    fun `should return 401 when no token provided for analytics`() {
        mockMvc.get("/admin/analytics/top-products").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 403 when USER role tries to access analytics`() {
        val testMember = Member(2L, "user@email.com", "password", "User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.get("/admin/analytics/top-products") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `should return empty list when no cart data exists`() {
        val testMember = Member(1L, "admin@email.com", "password", "Admin", Role.ADMIN)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.get("/admin/analytics/top-products") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun `should return top products analytics with correct ranking`() {
        val testMember = Member(1L, "admin@email.com", "password", "Admin", Role.ADMIN)
        val validToken = tokenService.generateToken(testMember)

        jdbcTemplate.update(
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (1, 1, 1, CURRENT_TIMESTAMP)
        """,
        )
        jdbcTemplate.update(
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (2, 1, 2, CURRENT_TIMESTAMP)
        """,
        )
        jdbcTemplate.update(
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (1, 2, 1, CURRENT_TIMESTAMP)
        """,
        )

        mockMvc.get("/admin/analytics/top-products") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].productName") { value("Product A") }
            jsonPath("$[0].addedCount") { value(3) }
            jsonPath("$[1].productName") { value("Product B") }
            jsonPath("$[1].addedCount") { value(1) }
        }
    }

    @Test
    fun `should return 401 when no token provided for active users analytics`() {
        mockMvc.get("/admin/analytics/active-users").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 403 when USER role tries to access active users analytics`() {
        val testMember = Member(2L, "user@email.com", "password", "User", Role.USER)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.get("/admin/analytics/active-users") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `should return empty list when no users have cart activity`() {
        val testMember = Member(1L, "admin@email.com", "password", "Admin", Role.ADMIN)
        val validToken = tokenService.generateToken(testMember)

        mockMvc.get("/admin/analytics/active-users") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun `should return unique active users from last 7 days`() {
        val testMember = Member(1L, "admin@email.com", "password", "Admin", Role.ADMIN)
        val validToken = tokenService.generateToken(testMember)

        jdbcTemplate.update(
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (1, 1, 1, CURRENT_TIMESTAMP)
        """,
        )
        jdbcTemplate.update(
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (1, 2, 1, CURRENT_TIMESTAMP)
        """,
        )
        jdbcTemplate.update(
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (2, 1, 1, CURRENT_TIMESTAMP)
        """,
        )

        mockMvc.get("/admin/analytics/active-users") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].memberId") { value(1) }
            jsonPath("$[0].memberName") { value("Admin") }
            jsonPath("$[0].memberEmail") { value("admin@email.com") }
            jsonPath("$[1].memberId") { value(2) }
            jsonPath("$[1].memberName") { value("User") }
            jsonPath("$[1].memberEmail") { value("user@email.com") }
        }
    }
}
