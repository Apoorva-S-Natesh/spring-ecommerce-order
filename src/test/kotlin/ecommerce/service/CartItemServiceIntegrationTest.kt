package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductOptionRepository
import ecommerce.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartItemServiceIntegrationTest {
    @Autowired
    private lateinit var cartItemService: CartItemService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var productOptionRepository: ProductOptionRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    private lateinit var testMember: Member
    private lateinit var testCart: Cart
    private lateinit var testProduct: Product
    private lateinit var testProductOption: ProductOption

    @BeforeEach
    fun setUp() {
        testMember = memberRepository.findById(1L).orElseThrow { RuntimeException("Test member not found") }
        testCart = cartRepository.findById(1L).orElseThrow { RuntimeException("Test cart not found") }
        testProduct = productRepository.findById(1L).orElseThrow { RuntimeException("Test product not found") }
        testProductOption = productOptionRepository.findById(1L).orElseThrow { RuntimeException("Test product option not found") }
    }

    @Test
    fun `addCartItem should create new cart item successfully`() {
        val request =
            AddToCartRequest(
                productOptionId = testProductOption.id!!,
                newProductOptionQuantity = 3,
                cartItemId = 0L,
                cartId = testCart.id!!,
            )

        val result = cartItemService.saveCartItem(request, testCart.id!!)

        assertThat(result).isNotNull()
        assertThat(result.id).isNotNull()
        assertThat(result.cart.id).isEqualTo(testCart.id)
        assertThat(result.productOption.id).isEqualTo(testProductOption.id)
        assertThat(result.quantity).isEqualTo(4)

        val savedItem = cartItemRepository.findById(result.id!!).orElse(null)
        assertThat(savedItem).isNotNull()
        assertThat(savedItem.quantity).isEqualTo(4)
    }

    @Test
    fun `addCartItem should update existing cart item when product option already in cart`() {
        val initialRequest =
            AddToCartRequest(
                productOptionId = testProductOption.id!!,
                newProductOptionQuantity = 2,
                cartItemId = 4L,
                cartId = testCart.id!!,
            )
        cartItemService.saveCartItem(initialRequest, testCart.id!!)

        val updateRequest =
            AddToCartRequest(
                productOptionId = testProductOption.id!!,
                newProductOptionQuantity = 3,
                cartItemId = 4L,
                cartId = testCart.id!!,
            )
        val result = cartItemService.saveCartItem(updateRequest, testCart.id!!)

        assertThat(result.quantity).isEqualTo(6)

        val cartItems = cartItemRepository.findByCartId(testCart.id!!)
        assertThat(cartItems.size).isEqualTo(2)
        assertThat(cartItems[0].quantity).isEqualTo(6)
    }

    @Test
    fun `deleteCartItemById should remove cart item successfully`() {
        val request =
            AddToCartRequest(
                productOptionId = testProductOption.id!!,
                newProductOptionQuantity = 2,
                cartItemId = 0L,
                cartId = testCart.id!!,
            )
        val createdItem = cartItemService.saveCartItem(request, testCart.id!!)

        cartItemService.deleteCartItemById(createdItem.id!!, testCart.id!!)

        val deletedItem = cartItemRepository.findById(createdItem.id!!).orElse(null)
        assertThat(deletedItem).isNull()
    }

    @Test
    fun `deleteAllCartItemsByCartId should remove all cart items`() {
        val itemsBefore = cartItemRepository.findByCartId(1)
        assertThat(itemsBefore.size).isEqualTo(2)

        cartItemService.deleteAllCartItemsByCartId(1)

        val itemsAfter = cartItemRepository.findByCartId(1)
        assertThat(itemsAfter.size).isEqualTo(0)
    }
}
