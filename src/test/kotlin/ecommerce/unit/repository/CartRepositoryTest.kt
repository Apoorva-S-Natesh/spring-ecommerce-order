package ecommerce.unit.repository

import ecommerce.model.Cart
import ecommerce.repository.CartRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.time.LocalDateTime

class CartRepositoryTest {
    private val dataSource =
        SingleConnectionDataSource(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;INIT=CREATE SCHEMA IF NOT EXISTS testdb;DB_CLOSE_ON_EXIT=FALSE",
            true,
        ).apply {
            setUsername("sa")
            setPassword("")
        }
    private val jdbcTemplate = JdbcTemplate(dataSource)
    private val cartRepository = CartRepository(jdbcTemplate)

    init {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE")
        jdbcTemplate.execute("DROP TABLE IF EXISTS cart CASCADE")
        jdbcTemplate.execute("DROP TABLE IF EXISTS products CASCADE")
        jdbcTemplate.execute("DROP TABLE IF EXISTS members CASCADE")
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE")

        jdbcTemplate.execute(
            """
            CREATE TABLE members (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                name VARCHAR(100) NOT NULL,
                role VARCHAR(20) DEFAULT 'USER',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent(),
        )

        jdbcTemplate.execute(
            """
            CREATE TABLE products (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255),
                price DOUBLE,
                imageUrl VARCHAR(255)
            )
            """.trimIndent(),
        )

        jdbcTemplate.execute(
            """
            CREATE TABLE cart (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                member_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                quantity INT NOT NULL DEFAULT 1,
                added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent(),
        )

        jdbcTemplate.execute("INSERT INTO members (id, email, password, name) VALUES (1, 'test@example.com', 'password', 'Test User')")
        jdbcTemplate.execute("INSERT INTO members (id, email, password, name) VALUES (2, 'test2@example.com', 'password', 'Test User 2')")
        jdbcTemplate.execute("INSERT INTO products (id, name, price) VALUES (1, 'Product 1', 10.0)")
        jdbcTemplate.execute("INSERT INTO products (id, name, price) VALUES (2, 'Product 2', 20.0)")
    }

    @Test
    fun `should save and retrieve cart items - valid attempt`() {
        val now = LocalDateTime.now()
        val cart =
            Cart(
                memberId = 1L,
                productId = 2L,
                quantity = 3,
                addedAt = now,
            )

        val saved = cartRepository.save(cart)

        assertThat(saved.memberId).isEqualTo(cart.memberId)
        assertThat(saved.productId).isEqualTo(cart.productId)
        assertThat(saved.quantity).isEqualTo(cart.quantity)
        assertThat(saved.addedAt).isNotNull()
    }

    @Test
    fun `should find cart items by userId`() {
        val cart1 = Cart(memberId = 1L, productId = 1L, quantity = 2)
        val cart2 = Cart(memberId = 1L, productId = 2L, quantity = 1)
        val cart3 = Cart(memberId = 2L, productId = 1L, quantity = 3)

        cartRepository.save(cart1)
        cartRepository.save(cart2)
        cartRepository.save(cart3)

        val userCarts = cartRepository.findByUserId(1L)

        assertThat(userCarts).hasSize(2)
        assertThat(userCarts.map { it.productId }).containsExactlyInAnyOrder(1L, 2L)
    }

    @Test
    fun `should find specific cart item by userId and productId`() {
        val cart = Cart(memberId = 1L, productId = 2L, quantity = 3)
        cartRepository.save(cart)

        val found = cartRepository.findByUserIdAndProductId(1L, 2L)

        assertThat(found).isNotNull
        assertThat(found?.memberId).isEqualTo(1L)
        assertThat(found?.productId).isEqualTo(2L)
        assertThat(found?.quantity).isEqualTo(3)
    }
}
