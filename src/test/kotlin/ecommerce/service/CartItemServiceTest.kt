package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.model.Role
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductOptionRepository
import ecommerce.repository.ProductRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime
import java.util.Optional

class CartItemServiceTest {
    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var cartItemRepository: CartItemRepository

    @Mock
    private lateinit var productOptionRepository: ProductOptionRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var productOptionService: ProductOptionService

    @Mock
    private lateinit var productService: ProductService

    private lateinit var cartItemService: CartItemService

    private lateinit var testMember: Member
    private lateinit var testCart: Cart
    private lateinit var testProduct: Product
    private lateinit var testProductOption: ProductOption
    private lateinit var testCartItem: CartItem

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        cartItemService =
            CartItemService(
                cartRepository,
                cartItemRepository,
                productOptionRepository,
                productRepository,
                productOptionService,
                productService,
            )

        testMember = Member("test@example.com", "password", "Test User", Role.USER, id = 1L)
        testCart = Cart(member = testMember, id = 1L)
        testProduct = Product("Test Product", 99.99, 10, "https://example.com/image.jpg", id = 1L)
        testProductOption = ProductOption("Red", 5, testProduct, id = 1L)
        testCartItem = CartItem(testCart, testProductOption, 2, LocalDateTime.now(), id = 1L)
    }

    @Test
    fun `addCartItem should create new cart item when item doesn't exist`() {
        val request = AddToCartRequest(1L, 3, 1L, 1L)
        val cartId = 1L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))
        `when`(productOptionRepository.findById(request.productOptionId)).thenReturn(Optional.of(testProductOption))
        `when`(cartItemRepository.findByCartAndProductOption(testCart, testProductOption)).thenReturn(null)
        `when`(cartItemRepository.save(any(CartItem::class.java))).thenReturn(testCartItem)

        val result = cartItemService.saveCartItem(request, cartId)

        assertNotNull(result)
        verify(cartRepository).findById(cartId)
        verify(productOptionRepository).findById(request.productOptionId)
        verify(cartItemRepository).findByCartAndProductOption(testCart, testProductOption)
        verify(cartItemRepository).save(any(CartItem::class.java))
    }

    @Test
    fun `addCartItem should update existing cart item when item exists`() {
        val request = AddToCartRequest(1L, 3, 1L, 1L)
        val cartId = 1L
        val existingCartItem = CartItem(testCart, testProductOption, 2, LocalDateTime.now(), id = 1L)

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))
        `when`(productOptionRepository.findById(request.productOptionId)).thenReturn(Optional.of(testProductOption))
        `when`(cartItemRepository.findByCartAndProductOption(testCart, testProductOption)).thenReturn(existingCartItem)
        `when`(cartItemRepository.save(any(CartItem::class.java))).thenReturn(existingCartItem)

        val result = cartItemService.saveCartItem(request, cartId)

        assertNotNull(result)
        verify(cartItemRepository).save(any(CartItem::class.java))
    }

    @Test
    fun `addCartItem should throw NotFoundException when cart not found`() {
        val request = AddToCartRequest(1L, 3, 1L, 1L)
        val cartId = 999L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            cartItemService.saveCartItem(request, cartId)
        }
        verify(cartRepository).findById(cartId)
    }

    @Test
    fun `addCartItem should throw NotFoundException when product option not found`() {
        val request = AddToCartRequest(999L, 3, 1L, 1L)
        val cartId = 1L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))
        `when`(productOptionRepository.findById(request.productOptionId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            cartItemService.saveCartItem(request, cartId)
        }
    }

    @Test
    fun `deleteCartItemById should delete cart item successfully`() {
        val cartItemId = 1L
        val cartId = 1L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))
        `when`(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem))

        cartItemService.deleteCartItemById(cartItemId, cartId)

        verify(cartRepository).findById(cartId)
        verify(cartItemRepository).findById(cartItemId)
        verify(cartItemRepository).deleteById(cartItemId)
    }

    @Test
    fun `deleteCartItemById should throw NotFoundException when cart not found`() {
        val cartItemId = 1L
        val cartId = 999L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            cartItemService.deleteCartItemById(cartItemId, cartId)
        }
    }

    @Test
    fun `deleteCartItemById should throw NotFoundException when cart item not found`() {
        val cartItemId = 999L
        val cartId = 1L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))
        `when`(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            cartItemService.deleteCartItemById(cartItemId, cartId)
        }
    }

    @Test
    fun `deleteAllCartItemsByCartId should delete all cart items successfully`() {
        val cartId = 1L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))

        cartItemService.deleteAllCartItemsByCartId(cartId)

        verify(cartRepository).findById(cartId)
        verify(cartItemRepository).deleteAllByCartId(cartId)
    }

    @Test
    fun `deleteAllCartItemsByCartId should throw NotFoundException when cart not found`() {
        val cartId = 999L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            cartItemService.deleteAllCartItemsByCartId(cartId)
        }
    }
}
