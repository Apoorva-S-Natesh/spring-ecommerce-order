package ecommerce.repository

import ecommerce.dto.product.ProductPatchRequest
import ecommerce.exception.NotFoundException
import ecommerce.model.Product
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class ProductRepository(private val jdbc: JdbcTemplate) {
    private val productRowMapper =
        RowMapper<Product> { rs: ResultSet, _ ->
            Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("imageUrl"),
            )
        }

    fun findAll(): List<Product> {
        val sql = "SELECT * FROM products"
        return jdbc.query(sql, productRowMapper)
    }

    fun findById(id: Long): Product {
        val sql = "SELECT * from products where ID = ?"
        return try {
            jdbc.queryForObject(sql, productRowMapper, id) ?: throw NotFoundException("Product with Id: $id. Not found.")
        } catch (_: org.springframework.dao.EmptyResultDataAccessException) {
            throw NotFoundException("Product with Id: $id. Not found.")
        }
    }

    fun save(product: Product): Product {
        val sql = "insert into products (name, price, imageUrl) values (?, ?, ?)"
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbc.update({
            it.prepareStatement(sql, arrayOf("id")).apply {
                setString(1, product.name)
                setDouble(2, product.price)
                setString(3, product.imageUrl)
            }
        }, keyHolder)
        return product.copy(id = keyHolder.key!!.toLong())
    }

    fun update(
        id: Long,
        product: Product,
    ): Product {
        val sql = "UPDATE products SET name = ?, price = ?, imageUrl = ? WHERE id = ?"
        val rowsAffected = jdbc.update(sql, product.name, product.price, product.imageUrl, id)
        if (rowsAffected == 0) {
            throw NotFoundException("Product with Id: $id. Not found.")
        }
        return product.copy(id = id)
    }

    fun delete(id: Long) {
        val sql = "DELETE FROM products WHERE ID = ?"
        val rowsAffected = jdbc.update(sql, id)
        if (rowsAffected == 0) {
            throw NotFoundException("Product with Id: $id. Not found.")
        }
    }

    fun patch(
        id: Long,
        patchProduct: ProductPatchRequest,
    ): Product {
        val existing = findById(id)
        val updatedName = patchProduct.name ?: existing.name
        val updatedPrice = patchProduct.price ?: existing.price
        val updatedImageUrl = patchProduct.imageUrl ?: existing.imageUrl

        val updatedProduct = Product(id, updatedName, updatedPrice, updatedImageUrl)
        val sql = "UPDATE products SET name = ?, price = ?, imageUrl = ? WHERE id = ?"
        jdbc.update(sql, updatedName, updatedPrice, updatedImageUrl, id)

        return updatedProduct
    }

    fun existsByName(name: String): Boolean {
        val sql = "SELECT EXISTS(SELECT 1 FROM products WHERE name = ?)"
        val count = jdbc.queryForObject(sql, Int::class.java, name) ?: 0
        return count > 0
    }
}
