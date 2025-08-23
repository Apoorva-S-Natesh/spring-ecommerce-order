package ecommerce.service

import ecommerce.dto.PaymentResponse
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.model.ProductOption
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductOptionRepository
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    lateinit var orderService: OrderService

    @MockitoBean
    lateinit var paymentService: PaymentService

    @Autowired
    lateinit var productOptionRepository: ProductOptionRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var cartItemRepository: CartItemRepository

    lateinit var member: Member
    lateinit var option: ProductOption
    lateinit var cartItem: CartItem

    @BeforeEach
    fun setUp() {
        member = memberRepository.findAll().first { it.email == "test@example.com" }
        option = productOptionRepository.findById(2L).get()
        cartItem = cartItemRepository.findAll().first()
    }

    @Test
    fun `createOrder should place order successfully with valid request`() {
        val mockPaymentResponse =
            PaymentResponse(
                id = "pi_123",
                amount = 1000L,
                status = "succeeded",
                paymentMethod = "pm_visa_card",
                currency = "USD",
                declineCode = null,
            )
        `when`(paymentService.processPayment(any())).thenReturn(mockPaymentResponse)
        val orderResponse = orderService.createOrder(member.id!!, mockPaymentResponse, option, 1)
        assertNotNull(orderResponse.id)
        assertThat(orderResponse.amount).isEqualTo(1000)
    }

    @Test
    fun `createOrder should throw exception with invalid member`() {
        val paymentResponse =
            PaymentResponse(
                id = "pi_123",
                amount = 1000L,
                status = "succeeded",
                paymentMethod = "pm_visa_card",
                currency = "USD",
                declineCode = null,
            )

        assertThrows<EntityNotFoundException> {
            orderService.createOrder(999L, paymentResponse, option, 1)
        }
    }
}
