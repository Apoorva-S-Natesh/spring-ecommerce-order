package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.repository.*
import org.junit.jupiter.api.Assertions.*
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

        assertNotNull(result)
        assertNotNull(result.id)
        assertEquals(testCart.id, result.cart.id)
        assertEquals(testProductOption.id, result.productOption.id)
        assertEquals(4, result.quantity)

        val savedItem = cartItemRepository.findById(result.id!!).orElse(null)
        assertNotNull(savedItem)
        assertEquals(4, savedItem.quantity)
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

        assertEquals(6, result.quantity)

        val cartItems = cartItemRepository.findByCartId(testCart.id!!)
        assertEquals(1, cartItems.size)
        assertEquals(6, cartItems[0].quantity)
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
        assertNull(deletedItem)
    }

    @Test
    fun `deleteAllCartItemsByCartId should remove all cart items`() {
        val newProduct = Product("New Product", 50.0, 20, "https://example.com/image.jpg")
        val savedProduct = productRepository.save(newProduct)

        val newProductOption = ProductOption("Green", 15, savedProduct)
        val savedProductOption = productOptionRepository.save(newProductOption)

        val request = AddToCartRequest(savedProductOption.id!!, 1, 0L, testCart.id!!)
        cartItemService.saveCartItem(request, testCart.id!!)

        val itemsBefore = cartItemRepository.findByCartId(testCart.id!!)
        assertEquals(2, itemsBefore.size)

        cartItemService.deleteAllCartItemsByCartId(testCart.id!!)

        val itemsAfter = cartItemRepository.findByCartId(testCart.id!!)
        assertEquals(0, itemsAfter.size)
    }
}
