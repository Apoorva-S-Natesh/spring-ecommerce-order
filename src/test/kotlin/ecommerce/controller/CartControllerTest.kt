package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.dto.cartItem.CartItemResponse
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.CartItemService
import ecommerce.service.CartService
import ecommerce.utils.ResponseMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class CartControllerTest {
    @Mock
    private lateinit var cartService: CartService

    @Mock
    private lateinit var cartItemService: CartItemService

    private lateinit var cartController: CartController
    private lateinit var cartItemController: CartItemController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        cartController = CartController(cartService, cartItemService)
        cartItemController = CartItemController(cartItemService)
    }

    @Test
    fun `should return cart by user id successfully`() {
        val userId = 1L
        val testMember = Member("test@email.com", "password", "Test User", Role.USER, id = userId)
        val testCart = Cart(member = testMember, id = 1L)

        `when`(cartService.getCartByUserId(userId)).thenReturn(ResponseMapper.cartToResponse(testCart))

        val response = cartController.getCart(userId)

        assertEquals(userId, response.memberId)
        verify(cartService, times(1)).getCartByUserId(userId)
    }

    @Test
    fun `should throw exception when cart service fails`() {
        val userId = 999L
        `when`(cartService.getCartByUserId(userId)).thenThrow(RuntimeException("Cart not found"))

        assertThrows(RuntimeException::class.java) {
            cartController.getCart(userId)
        }
        verify(cartService, times(1)).getCartByUserId(userId)
    }

    @Test
    fun `should return cart items when requested`() {
        val cartId = 1L
        val userId = 1L
        val emptyList = emptyList<CartItemResponse>()

        `when`(cartItemService.getCartItemsByCartId(cartId, userId)).thenReturn(emptyList)

        val result =
            cartItemController.getCartItemsByCartId(
                cartId,
                AuthenticatedUser(userId, Role.USER, "test@email.com", "Test User"),
            )

        assertEquals(emptyList, result)
        verify(cartItemService, times(1)).getCartItemsByCartId(cartId, userId)
    }
}
