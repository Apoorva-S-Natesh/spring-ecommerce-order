package ecommerce.integration

import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class MemberControllerIntegrationTest {
    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return 400 for invalid empty email`() {
        mockMvc.post("/api/members/register") {
            contentType = APPLICATION_JSON
            content = """{"email": "", "password": "123456", "name": "Test User"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 for invalid email format`() {
        mockMvc.post("/api/members/register") {
            contentType = APPLICATION_JSON
            content = """{"email": "invalid-email", "password": "123456", "name": "Test User"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 for short password`() {
        mockMvc.post("/api/members/register") {
            contentType = APPLICATION_JSON
            content = """{"email": "test@email.com", "password": "123", "name": "Test User"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 for empty name`() {
        mockMvc.post("/api/members/register") {
            contentType = APPLICATION_JSON
            content = """{"email": "test@email.com", "password": "123456", "name": ""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 for too short name`() {
        mockMvc.post("/api/members/register") {
            contentType = APPLICATION_JSON
            content = """{"email": "test@email.com", "password": "123456", "name": "A"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 for empty login email`() {
        mockMvc.post("/api/members/login") {
            contentType = APPLICATION_JSON
            content = """{"email": "", "password": "123456"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 for empty login password`() {
        mockMvc.post("/api/members/login") {
            contentType = APPLICATION_JSON
            content = """{"email": "test@email.com", "password": ""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 200 - valid token of ADMIN member`() {
        val testMember = Member(1L, "jin@gmail.com", "pw1234", "jin", Role.ADMIN)
        val validToken = tokenService.generateToken(testMember)
        mockMvc.get("/admin") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should return 200 - USER can access cart`() {
        val testMember = Member(1L, "jin@gmail.com", "pw1234", "jin")
        val validToken = tokenService.generateToken(testMember)
        mockMvc.get("/api/cart-items") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should return 401 - invalid token`() {
        mockMvc.get("/admin") {
            header("Authorization", "Bearer invalid-token")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 401 - no token provided`() {
        mockMvc.get("/admin").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 403 - USER role cannot access admin endpoint`() {
        val testMember = Member(1L, "user@gmail.com", "pw1234", "user", Role.USER)
        val validToken = tokenService.generateToken(testMember)
        mockMvc.get("/admin") {
            header("Authorization", "Bearer $validToken")
        }.andExpect {
            status { isForbidden() }
        }
    }
}
