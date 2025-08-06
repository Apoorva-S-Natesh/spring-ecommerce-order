package ecommerce.controller

import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class CartStatisticsControllerTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tokenService: TokenService

    private fun createAdminToken(): String {
        val adminMember = Member("admin@test.com", "password", "Admin User", Role.ADMIN, null, 1L)
        return tokenService.generateToken(adminMember)
    }

    private fun createUserToken(): String {
        val userMember = Member("user@test.com", "password", "Regular User", Role.USER, null, 2L)
        return tokenService.generateToken(userMember)
    }

    @Test
    fun `should return top product's name`() {
        val adminToken = createAdminToken()

        mockMvc.get("/admin/analytics/top-products") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].productName") { value("Bike") }
        }
    }

    @Test
    fun `should return active member information`() {
        val adminToken = createAdminToken()

        mockMvc.get("/admin/analytics/active-users") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].memberId") { value(3) }
            jsonPath("$[0].memberEmail") { value("test2@example.com") }
            jsonPath("$[0].memberName") { value("Test User2") }
        }
    }
}
