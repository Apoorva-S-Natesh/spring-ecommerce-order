package ecommerce.endtoend

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

    private fun getBaseUrl() = "http://localhost:$port/api"

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun getProducts() {
        val response =
            RestAssured.given()
                .accept(ContentType.JSON)
                .`when`().get("${getBaseUrl()}/products")
                .then().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val products = response.body().jsonPath().getList<Any>("")
        assertThat(products).isNotEmpty()
        assertThat(products.size).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun getProduct() {
        val response =
            RestAssured.given()
                .get("${getBaseUrl()}/products/1")
                .then().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("name")).isEqualTo("Test Product")
        assertThat(response.body().jsonPath().getDouble("price")).isEqualTo(100.0)
    }

    @Test
    fun getProduct_notFound() {
        val response =
            RestAssured.given()
                .get("${getBaseUrl()}/products/999999")
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
                "imageUrl" to "http://example.com/image.jpg",
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(newProduct)
                .`when`()
                .post("${getBaseUrl()}/products")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()

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
                "imageUrl" to "http://example.com/initial.jpg",
            )

        val createdResponse =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(newProduct)
                .`when`()
                .post("${getBaseUrl()}/products")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()

        val productId = createdResponse.body().jsonPath().getLong("id")

        val updatedProduct =
            mapOf(
                "name" to "Updated Product",
                "price" to 150.0,
                "imageUrl" to "http://example.com/updated.jpg",
            )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updatedProduct)
            .`when`()
            .put("${getBaseUrl()}/products/$productId")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("name", equalTo("Updated Product"))
            .body("price", equalTo(150.0f))
    }

    @Test
    fun patchProduct() {
        val newProduct =
            mapOf(
                "name" to "Initial Product",
                "price" to 100.0,
                "imageUrl" to "http://example.com/initial.jpg",
            )

        val createdResponse =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(newProduct)
                .`when`()
                .post("${getBaseUrl()}/products")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()

        val productId = createdResponse.body().jsonPath().getLong("id")
        val patch =
            mapOf(
                "name" to "Patched Product",
            )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(patch)
            .`when`()
            .patch("${getBaseUrl()}/products/$productId")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("name", equalTo("Patched Product"))
    }

    @Test
    fun deleteProduct() {
        val newProduct =
            mapOf(
                "name" to "Delete Test",
                "price" to 99.99,
                "imageUrl" to "http://example.com/delete-me.jpg",
            )

        val createdResponse =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(newProduct)
                .`when`()
                .post("${getBaseUrl()}/products")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()

        val productId = createdResponse.body().jsonPath().getLong("id")
        val deleteResponse =
            RestAssured.given()
                .`when`()
                .delete("${getBaseUrl()}/products/$productId")
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
                .get("${getBaseUrl()}/products/$productId")
                .then()
                .extract()

        assertThat(getResponse.statusCode())
            .withFailMessage("Expected status code 404 but was ${getResponse.statusCode()}")
            .isEqualTo(HttpStatus.NOT_FOUND.value())
    }
}
