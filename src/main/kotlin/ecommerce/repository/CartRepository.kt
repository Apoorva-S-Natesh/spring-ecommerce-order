package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class CartRepository(private val jdbc: JdbcTemplate) {
    private val cartRowMapper =
        RowMapper<Cart> { rs, _ ->
            Cart(
                id = rs.getLong("id"),
                memberId = rs.getLong("member_id"),
                productId = rs.getLong("product_id"),
                quantity = rs.getInt("quantity"),
                addedAt = rs.getTimestamp("added_at")?.toLocalDateTime(),
            )
        }

    fun save(cart: Cart): Cart {
        val sql =
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (?, ?, ?, ?)
            """.trimIndent()

        val addedAt = cart.addedAt ?: LocalDateTime.now()

        jdbc.update(
            sql,
            cart.memberId,
            cart.productId,
            cart.quantity,
            Timestamp.valueOf(addedAt),
        )
        return cart.copy(addedAt = addedAt)
    }

    fun findByUserId(userId: Long): List<Cart> {
        val sql = "SELECT * FROM cart WHERE member_id = ?"
        return jdbc.query(sql, cartRowMapper, userId)
    }

    fun findByUserIdAndProductId(
        userId: Long,
        productId: Long,
    ): Cart? {
        val sql = "SELECT * FROM cart WHERE member_id = ? AND product_id = ?"
        val results = jdbc.query(sql, cartRowMapper, userId, productId)
        return results.firstOrNull()
    }

    fun update(cart: Cart): Cart {
        val sql =
            """
            UPDATE cart 
            SET quantity = ?, added_at = ?
            WHERE member_id = ? AND product_id = ?
            """.trimIndent()

        val addedAt = cart.addedAt ?: LocalDateTime.now()

        jdbc.update(
            sql,
            cart.quantity,
            Timestamp.valueOf(addedAt),
            cart.memberId,
            cart.productId,
        )
        return cart.copy(addedAt = addedAt)
    }

    fun deleteByUserIdAndProductId(
        userId: Long,
        productId: Long,
    ) {
        val sql = "DELETE FROM cart WHERE member_id = ? AND product_id = ?"
        jdbc.update(sql, userId, productId)
    }

    fun deleteByUserId(userId: Long) {
        val sql = "DELETE FROM cart WHERE member_id = ?"
        jdbc.update(sql, userId)
    }

    fun findAll(): List<Cart> {
        val sql = "SELECT * FROM cart"
        return jdbc.query(sql, cartRowMapper)
    }
}
