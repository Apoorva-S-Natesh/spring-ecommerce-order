package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.dto.cart.AddToCartRequest
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
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

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Test
    fun `cart item should be updated to cart`() {
        val addToCartRequest =
            AddToCartRequest(
                productOptionId = 1,
                newProductOptionQuantity = 3,
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
        val member =
            memberRepository.save(
                Member(
                    "test3@example.com",
                    "\$2a\$10\$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.",
                    "Test User3",
                    Role.USER,
                    null,
                ),
            )
        val cart = cartRepository.save(Cart(member))

        val addToCartRequest =
            AddToCartRequest(
                productOptionId = 2,
                newProductOptionQuantity = 7,
                cartItemId = null,
                cartId = cart.id!!,
            )

        val response =
            cartItemController.addToCart(
                3,
                addToCartRequest,
                AuthenticatedUser(member.id!!, Role.USER, "test@example.com", "Test User"),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.quantity).isEqualTo(7)
        assertThat(response.body?.productOptionResponse?.id).isEqualTo(2)
        assertThat(response.body?.productOptionResponse?.name).isEqualTo("Red")
        assertThat(response.body?.cartId).isEqualTo(cart.id)

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
