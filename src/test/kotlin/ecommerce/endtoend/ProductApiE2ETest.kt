package ecommerce.endtoend

import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.TokenService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ProductApiE2ETest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var tokenService: TokenService

    private lateinit var adminToken: String

    private fun getBaseUrl() = "http://localhost:$port/api"

    @BeforeEach
    fun setUp() {
        val adminUser = Member(
            email = "admin@example.com", 
            password = "password", 
            name = "Admin User", 
            role = Role.ADMIN,
            id = 2L
        )
        adminToken = tokenService.generateToken(adminUser)
    }

    @Test
    fun getProducts() {
        val response =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .accept(ContentType.JSON)
                .`when`().get("${getBaseUrl()}/admin/products")
                .then().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val products = response.body().jsonPath().getList<Any>("content")
        assertThat(products).isNotEmpty()
        assertThat(products.size).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun getProduct() {
        val response =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .get("${getBaseUrl()}/admin/products/1")
                .then().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("name")).isEqualTo("Car")
        assertThat(response.body().jsonPath().getDouble("price")).isEqualTo(1000.0)
    }

    @Test
    fun getProduct_notFound() {
        val response =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .get("${getBaseUrl()}/admin/products/999999")
                .then().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun createProduct() {
        val productName = "P_${System.currentTimeMillis() % 10000}".take(15)
        val newProduct =
            mapOf(
                "name" to productName,
                "price" to 100.0,
                "quantity" to 10,
                "imageUrl" to "http://example.com/image.jpg"
            )

        val response =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .contentType(ContentType.JSON)
                .body(newProduct)
                .`when`()
                .post("${getBaseUrl()}/admin/products")
                .then()
                .extract()

        println("Response status: ${response.statusCode()}")
        println("Response body: ${response.body().asString()}")
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())

        val responseBody = response.body().jsonPath()
        assertThat(responseBody.getLong("id")).isNotNull()
        assertThat(responseBody.getString("name")).isEqualTo(productName)
        assertThat(responseBody.getDouble("price")).isEqualTo(100.0)
        assertThat(responseBody.getString("imageUrl")).isEqualTo("http://example.com/image.jpg")
    }

    @Test
    fun updateProduct() {
        val newProduct =
            mapOf(
                "name" to "Initial Product",
                "price" to 100.0,
                "quantity" to 10,
                "imageUrl" to "http://example.com/image.jpg"
            )

        val createdResponse =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .contentType(ContentType.JSON)
                .body(newProduct)
                .`when`()
                .post("${getBaseUrl()}/admin/products")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()

        val productId = createdResponse.body().jsonPath().getLong("id")

        val updatedProduct =
            mapOf(
                "name" to "Updated Product",
                "price" to 150.0,
                "quantity" to 15,
                "imageUrl" to "http://example.com/image.jpg"
            )

        RestAssured.given()
            .header("Authorization", "Bearer $adminToken")
            .contentType(ContentType.JSON)
            .body(updatedProduct)
            .`when`()
            .put("${getBaseUrl()}/admin/products/$productId")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("name", equalTo("Updated Product"))
            .body("price", equalTo(150.0f))
    }

    @Test
    fun deleteProduct() {
        val newProduct =
            mapOf(
                "name" to "Delete Test",
                "price" to 99.99,
                "quantity" to 5,
                "imageUrl" to "http://example.com/image.jpg"
            )

        val createdResponse =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .contentType(ContentType.JSON)
                .body(newProduct)
                .`when`()
                .post("${getBaseUrl()}/admin/products")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()

        val productId = createdResponse.body().jsonPath().getLong("id")
        val deleteResponse =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .`when`()
                .delete("${getBaseUrl()}/admin/products/$productId")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .extract()

        assertThat(deleteResponse.statusCode())
            .withFailMessage("Expected status code 204 but was ${deleteResponse.statusCode()}")
            .isEqualTo(HttpStatus.NO_CONTENT.value())
        
        val productCount =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE id = ?",
                { rs, _ -> rs.getInt(1) },
                productId,
            )
        assertThat(productCount).isZero()

        val getResponse =
            RestAssured.given()
                .header("Authorization", "Bearer $adminToken")
                .get("${getBaseUrl()}/admin/products/$productId")
                .then()
                .extract()

        assertThat(getResponse.statusCode())
            .withFailMessage("Expected status code 404 but was ${getResponse.statusCode()}")
            .isEqualTo(HttpStatus.NOT_FOUND.value())
    }
}