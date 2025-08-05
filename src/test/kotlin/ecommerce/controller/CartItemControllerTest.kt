package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.dto.cart.AddToCartRequest
import ecommerce.model.Role
import ecommerce.repository.CartItemRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class CartItemControllerTest {
    @Autowired
    private lateinit var cartItemController: CartItemController

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Test
    fun `cart item should be updated to cart`() {
        val addToCartRequest =
            AddToCartRequest(
                productOptionId = 1,
                newProductOptionQuantity = 7,
                cartItemId = 1,
                cartId = 1,
            )

        val response =
            cartItemController.updateCartItemForIncrement(
                1,
                1,
                addToCartRequest,
                AuthenticatedUser(1, Role.USER, "test@example.com", "Test User"),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `cart item should be updated to cart1`() {
        val addToCartRequest =
            AddToCartRequest(
                productOptionId = 1,
                newProductOptionQuantity = 5,
                cartItemId = 1,
                cartId = 1,
            )

        val response =
            cartItemController.addToCart(
                1,
                addToCartRequest,
                AuthenticatedUser(1, Role.USER, "test@example.com", "Test User"),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @Sql(statements = ["DELETE FROM cart_items"])
    fun `cart item should be added to cart`() {
        val addToCartRequest =
            AddToCartRequest(
                productOptionId = 2,
                newProductOptionQuantity = 7,
                cartItemId = 3,
                cartId = 1,
            )

        val response =
            cartItemController.addToCart(
                1,
                addToCartRequest,
                AuthenticatedUser(1, Role.USER, "test@example.com", "Test User"),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.quantity).isEqualTo(7)
        assertThat(response.body?.productOption?.id).isEqualTo(2)
        assertThat(response.body?.productOption?.name).isEqualTo("Red")
        assertThat(response.body?.cart?.id).isEqualTo(1)

        val savedCartItem = cartItemRepository.findById(response.body?.id!!)
        assertThat(savedCartItem).isPresent
        assertThat(savedCartItem.get().quantity).isEqualTo(7)
        assertThat(savedCartItem.get().productOption.name).isEqualTo("Red")
    }

    @Test
    fun `delete cart item from item id`() {
        val response = cartItemController.deleteCartItem(1, 1)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }
}
