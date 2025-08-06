package ecommerce.unit.model

import ecommerce.model.Product
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {
    private fun createTestProduct(
        name: String = "Test Product",
        imageUrl: String = VALID_IMAGE_URL,
    ) = Product(
        name = name,
        price = 10.0,
        quantity = 2,
        imageUrl = imageUrl,
    )

    private fun createTestProductInvalid(
        name: String = "Test Product",
        imageUrl: String = INVALID_IMAGE_URL,
    ) = Product(
        name = name,
        price = 10.0,
        quantity = 2,
        imageUrl = imageUrl,
    )

    @Test
    fun `throw exception - product name can not be empty`() {
        assertThrows<IllegalArgumentException> {
            createTestProduct(name = "")
        }
    }

    @Test
    fun `throw exception - product name can not be longer than 15`() {
        assertThrows<IllegalArgumentException> {
            createTestProduct(name = "a".repeat(16))
        }
    }

    @Test
    fun `throw exception - product option can not be empty`() {
    }

    @Test
    fun `throw exception - product name contains unsupported characters`() {
        assertThrows<IllegalArgumentException> {
            createTestProduct(name = "$")
        }
    }

    @Test
    fun `throw exception - image url does not start with allowed url`() {
        assertThrows<IllegalArgumentException> {
            createTestProductInvalid(name = "TestName")
        }
    }

    @Test
    fun `throw exception - product contains duplicate product options`() {
    }

    companion object {
        private const val VALID_IMAGE_URL = "http://example.com/image.jpg"
        private const val INVALID_IMAGE_URL = "hp://invalid"
    }
}
