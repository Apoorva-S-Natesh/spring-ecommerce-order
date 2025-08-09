package ecommerce.controller

import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.TokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {
    private lateinit var adminToken: String

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUpAdmin() {
        val adminMember = Member("admin@email.com", "adminpass", "Admin User", Role.ADMIN, null, 2L)
        adminToken = tokenService.generateToken(adminMember)
    }

    @Test
    fun `should return member for valid id`() {
        mockMvc.get("/api/admin/member/1") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.email") { value("test@example.com") }
            jsonPath("$.name") { value("Test User") }
        }
    }

    @Test
    fun `should get all members`() {
        mockMvc.get("/api/admin/member") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content.length()") { value(3) }
            jsonPath("$.totalElements") { value(3) }

            jsonPath("$.content[0].id") { value(1) }
            jsonPath("$.content[0].email") { value("test@example.com") }
            jsonPath("$.content[0].name") { value("Test User") }

            jsonPath("$.content[1].id") { value(2) }
            jsonPath("$.content[1].email") { value("admin@example.com") }
            jsonPath("$.content[1].name") { value("Admin User") }
        }
    }

    @Test
    fun `should update member successfully`() {
        mockMvc.put("/api/admin/member/1") {
            header("Authorization", "Bearer $adminToken")
            contentType = APPLICATION_JSON
            content = """{"email": "updated@email.com", "name": "Updated User"}"""
        }.andExpect {
            status { isOk() }
        }

        mockMvc.get("/api/admin/member/1") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.email") { value("updated@email.com") }
            jsonPath("$.name") { value("Updated User") }
        }
    }

    @Test
    fun `should return 404 when updating non-existent member`() {
        mockMvc.put("/api/admin/member/999") {
            header("Authorization", "Bearer $adminToken")
            contentType = APPLICATION_JSON
            content = """{"email": "test@email.com", "name": "Test User"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 400 for invalid update request`() {
        mockMvc.put("/api/admin/member/1") {
            header("Authorization", "Bearer $adminToken")
            contentType = APPLICATION_JSON
            content = """{"email": "invalid-email", "name": ""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should delete member successfully`() {
        mockMvc.delete("/api/admin/member/1") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/admin/member/1") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 404 when deleting non-existent member`() {
        mockMvc.delete("/api/admin/member/999") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isNotFound() }
        }
    }
}
