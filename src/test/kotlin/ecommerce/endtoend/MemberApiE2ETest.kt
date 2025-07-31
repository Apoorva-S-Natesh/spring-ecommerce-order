package ecommerce.endtoend

import ecommerce.dto.member.LoginRequest
import ecommerce.dto.member.RegisterRequest
import ecommerce.dto.member.TokenResponse
import ecommerce.exception.ErrorResponse
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class MemberApiE2ETest {
    @LocalServerPort
    private var port: Int = 0

    private fun getBaseUrl() = "http://localhost:$port/api"

    @Test
    fun `should register user successfully with valid data`() {
        val registerRequest =
            RegisterRequest(
                email = "newuser@test.com",
                password = "password123",
                name = "New User",
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .post("${getBaseUrl()}/members/register")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        val tokenResponse = response.body().`as`(TokenResponse::class.java)
        assertThat(tokenResponse.token).isNotBlank()
    }

    @Test
    fun `should return 400 when registering with duplicate email`() {
        val registerRequest =
            RegisterRequest(
                email = "duplicate@test.com",
                password = "password123",
                name = "Duplicate User",
            )
        val firstResponse =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .post("${getBaseUrl()}/members/register")
                .then()
                .extract()

        assertThat(firstResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .post("${getBaseUrl()}/members/register")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should login successfully with valid credentials`() {
        val registerRequest =
            RegisterRequest(
                email = "logintest@test.com",
                password = "password123",
                name = "Login Test User",
            )

        val registerResponse =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .post("${getBaseUrl()}/members/register")
                .then()
                .extract()

        assertThat(registerResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value())

        val loginRequest =
            LoginRequest(
                email = "logintest@test.com",
                password = "password123",
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("${getBaseUrl()}/members/login")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val tokenResponse = response.body().`as`(TokenResponse::class.java)
        assertThat(tokenResponse.token).isNotBlank()
    }

    @Test
    fun `should return 401 when login with invalid credentials`() {
        val loginRequest =
            LoginRequest(
                email = "nonexistent@test.com",
                password = "wrongpassword",
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("${getBaseUrl()}/members/login")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value())
        val errorResponse = response.body().`as`(ErrorResponse::class.java)
        assertThat(errorResponse.error).isEqualTo("Unauthorized")
        assertThat(errorResponse.message).isEqualTo("Invalid email or password")
    }

    @Test
    fun `should return 400 when register with invalid email format`() {
        val registerRequest =
            RegisterRequest(
                email = "invalid-email",
                password = "password123",
                name = "Test User",
            )

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .post("${getBaseUrl()}/members/register")
                .then()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }
}
