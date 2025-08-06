package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.ProductOptionRequest
import ecommerce.dto.ProductRequest
import ecommerce.model.Member
import ecommerce.model.Product
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class AdminControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun createAdminToken(): String {
        val adminMember = Member("admin@test.com", "password", "Admin User", Role.ADMIN, null, 1L)
        return tokenService.generateToken(adminMember)
    }

    private fun createUserToken(): String {
        val userMember = Member("user@test.com", "password", "Regular User", Role.USER, null, 2L)
        return tokenService.generateToken(userMember)
    }

    @Test
    fun `should get product by id successfully`() {
        val adminToken = createAdminToken()

        val productRequest =
            ProductRequest(
                name = "New Product",
                price = 99.99,
                quantity = 10,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        val createdProductResponse =
            mockMvc.post("/api/admin/products") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(productRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val productId =
            objectMapper.readTree(createdProductResponse.response.contentAsString)
                .get("id").asLong()

        mockMvc.get("/api/admin/products/$productId") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(productId) }
            jsonPath("$.name") { value("New Product") }
            jsonPath("$.price") { value(99.99) }
        }
    }

    @Test
    fun `should return 404 when getting non-existent product`() {
        val adminToken = createAdminToken()

        mockMvc.get("/api/admin/products/999999") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should get all products with pagination`() {
        val adminToken = createAdminToken()

        mockMvc.get("/api/admin/products") {
            header("Authorization", "Bearer $adminToken")
            param("page", "0")
            param("size", "10")
            param("sortBy", "name")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.pageable.pageNumber") { value(0) }
            jsonPath("$.pageable.pageSize") { value(10) }
        }
    }

    @Test
    fun `should create product successfully`() {
        val adminToken = createAdminToken()

        val productRequest =
            ProductRequest(
                name = "New Product",
                price = 149.99,
                quantity = 5,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        mockMvc.post("/api/admin/products") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(productRequest)
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.name") { value("New Product") }
            jsonPath("$.price") { value(149.99) }
            jsonPath("$.quantity") { value(5) }
            jsonPath("$.imageUrl") { value("https://example.com") }
            jsonPath("$.id") { exists() }
        }
    }

    @Test
    fun `should return 400 when creating product with invalid data`() {
        val adminToken = createAdminToken()

        val invalidProductRequest =
            ProductRequest(
                name = "",
                price = -10.0,
                quantity = 0,
                imageUrl = "invalid-url",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        mockMvc.post("/api/admin/products") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidProductRequest)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should update product successfully`() {
        val adminToken = createAdminToken()

        val originalProduct =
            ProductRequest(
                name = "OriginalProduct",
                price = 99.99,
                quantity = 10,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        val createdProductResponse =
            mockMvc.post("/api/admin/products") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(originalProduct)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val productId =
            objectMapper.readTree(createdProductResponse.response.contentAsString)
                .get("id").asLong()

        val updatedProduct =
            ProductRequest(
                name = "Updated Product",
                price = 199.99,
                quantity = 20,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        mockMvc.put("/api/admin/products/$productId") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedProduct)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Updated Product") }
            jsonPath("$.price") { value(199.99) }
            jsonPath("$.quantity") { value(20) }
        }
    }

    @Test
    fun `should delete product successfully`() {
        val adminToken = createAdminToken()

        val productRequest =
            ProductRequest(
                name = "DeleteProduct",
                price = 99.99,
                quantity = 10,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        val createdProductResponse =
            mockMvc.post("/api/admin/products") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(productRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val productId =
            objectMapper.readTree(createdProductResponse.response.contentAsString)
                .get("id").asLong()

        mockMvc.delete("/api/admin/products/$productId") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/admin/products/$productId") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should add product option successfully`() {
        val adminToken = createAdminToken()

        val productRequest =
            ProductRequest(
                name = "ProductOptions",
                price = 99.99,
                quantity = 10,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        val createdProductResponse =
            mockMvc.post("/api/admin/products") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(productRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val productId =
            objectMapper.readTree(createdProductResponse.response.contentAsString)
                .get("id").asLong()

        val product =
            objectMapper.readValue(
                createdProductResponse.response.contentAsString,
                Product::class.java,
            )

        val productOptionRequest =
            ProductOptionRequest(
                name = "Red Color",
                quantity = 5,
                productId = productId,
            )

        mockMvc.post("/api/admin/products/$productId/options") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(productOptionRequest)
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.name") { value("Red Color") }
            jsonPath("$.quantity") { value(5) }
            jsonPath("$.id") { exists() }
        }
    }

    @Test
    fun `should return 400 when adding product option with invalid data`() {
        val adminToken = createAdminToken()

        val productRequest =
            ProductRequest(
                name = "Test Product2",
                price = 99.99,
                quantity = 10,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        val createdProductResponse =
            mockMvc.post("/api/admin/products") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(productRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val productId =
            objectMapper.readTree(createdProductResponse.response.contentAsString)
                .get("id").asLong()

        val product =
            objectMapper.readValue(
                createdProductResponse.response.contentAsString,
                Product::class.java,
            )

        val invalidOptionRequest =
            ProductOptionRequest(
                name = "",
                quantity = 1,
                productId = productId,
            )

        mockMvc.post("/api/admin/products/$productId/options") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidOptionRequest)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 401 when accessing admin endpoints without token`() {
        mockMvc.get("/api/admin/products/1").andExpect {
            status { isUnauthorized() }
        }

        mockMvc.post("/api/admin/products") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 403 when accessing admin endpoints with non-admin token`() {
        val userToken = createUserToken()

        mockMvc.get("/api/admin/products/1") {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isForbidden() }
        }

        mockMvc.post("/api/admin/products") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `should handle pagination parameters correctly`() {
        val adminToken = createAdminToken()

        mockMvc.get("/api/admin/products") {
            header("Authorization", "Bearer $adminToken")
            param("page", "1")
            param("size", "5")
            param("sortBy", "price")
        }.andExpect {
            status { isOk() }
            jsonPath("$.pageable.pageNumber") { value(1) }
            jsonPath("$.pageable.pageSize") { value(5) }
        }
    }

    @Test
    fun `should handle default pagination parameters`() {
        val adminToken = createAdminToken()

        mockMvc.get("/api/admin/products") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.pageable.pageNumber") { value(0) }
            jsonPath("$.pageable.pageSize") { value(10) }
        }
    }

    @Test
    fun `should update product option successfully`() {
        val adminToken = createAdminToken()

        val productRequest =
            ProductRequest(
                name = "OptionUpdate",
                price = 99.99,
                quantity = 10,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        val createdProductResponse =
            mockMvc.post("/api/admin/products") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(productRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val productId =
            objectMapper.readTree(createdProductResponse.response.contentAsString)
                .get("id").asLong()

        val productOptionRequest =
            ProductOptionRequest(
                name = "Blue Color",
                quantity = 3,
                productId = productId,
            )

        val createdOptionResponse =
            mockMvc.post("/api/admin/products/$productId/options") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(productOptionRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val optionId =
            objectMapper.readTree(createdOptionResponse.response.contentAsString)
                .get("id").asLong()

        val updatedOptionRequest =
            ProductOptionRequest(
                name = "Green Color",
                quantity = 8,
                productId = productId,
            )

        mockMvc.put("/api/admin/products/$productId/options/$optionId") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedOptionRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Green Color") }
            jsonPath("$.quantity") { value(8) }
            jsonPath("$.id") { value(optionId) }
        }
    }

    @Test
    fun `should return 409 when adding duplicate product option name`() {
        val adminToken = createAdminToken()

        val productRequest =
            ProductRequest(
                name = "DuplicateTest",
                price = 99.99,
                quantity = 10,
                imageUrl = "https://example.com",
                productOptions = listOf(ProductOptionRequest("black", 20, 6L), ProductOptionRequest("Yellow", 10, 6L)),
            )

        val createdProductResponse =
            mockMvc.post("/api/admin/products") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(productRequest)
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val productId =
            objectMapper.readTree(createdProductResponse.response.contentAsString)
                .get("id").asLong()

        val firstOptionRequest =
            ProductOptionRequest(
                name = "Unique Color",
                quantity = 5,
                productId = productId,
            )

        mockMvc.post("/api/admin/products/$productId/options") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(firstOptionRequest)
        }.andExpect {
            status { isCreated() }
        }

        val duplicateOptionRequest =
            ProductOptionRequest(
                name = "Unique Color",
                quantity = 3,
                productId = productId,
            )

        mockMvc.post("/api/admin/products/$productId/options") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(duplicateOptionRequest)
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `should return 404 when updating option for non-existent product`() {
        val adminToken = createAdminToken()

        val updateRequest =
            ProductOptionRequest(
                name = "Test Color",
                quantity = 5,
                productId = 100,
            )

        mockMvc.put("/api/admin/products/999999/options/1") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isNotFound() }
        }
    }
}
