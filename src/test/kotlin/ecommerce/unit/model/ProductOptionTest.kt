package ecommerce.unit.model

import ecommerce.model.Product
import ecommerce.model.ProductOption
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ProductOptionTest {
    private fun createTestProduct() =
        Product(
            name = "TestProduct",
            price = 10.0,
            quantity = 5,
            imageUrl = "https://example.com/image.jpg"
        )

    @Test
    fun `Option names can not be empty`() {
        assertThrows<IllegalArgumentException> {
            ProductOption(name = "", quantity = 2, createTestProduct())
        }
    }

    @Test
    fun `Option names can not be longer than 50 characters`() {
        assertThrows<IllegalArgumentException> {
            ProductOption(name = "a".repeat(51), quantity = 2, createTestProduct())
        }
    }

    @Test
    fun `Option names can contain spaces`() {
        assertDoesNotThrow {
            ProductOption(name = "valid name", quantity = 2, createTestProduct())
        }
    }

    @Test
    fun `Option names throws Exception with unsupported characters`() {
        assertThrows<IllegalArgumentException> {
            ProductOption(name = "$$$$$$$", quantity = 2, createTestProduct())
        }
    }

    @Test
    fun `throws exception - Product option quantity less than minimum `() {
        assertThrows<IllegalArgumentException> {
            ProductOption(name = "Option", quantity = -2, createTestProduct())
        }
    }

    @Test
    fun `throws exception - Product option quantity greater than maximum `() {
        assertThrows<IllegalArgumentException> {
            ProductOption(name = "Option", quantity = 99999999 + 1, createTestProduct())
        }
    }
}
