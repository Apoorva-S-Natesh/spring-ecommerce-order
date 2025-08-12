package ecommerce.service

import ecommerce.dto.PaymentResponse
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.model.ProductOption
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.ProductOptionRepository
import ecommerce.repository.ProductRepository
import ecommerce.stripe.StripeClient
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    lateinit var orderService: OrderService

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
        val paymentResponse =
            PaymentResponse(
                id = "pi_123",
                amount = 1000L,
                status = "succeeded",
                paymentMethod = "pm_visa_card",
                currency = "USD",
                declineCode = null,
            )
        val orderResponse = orderService.createOrder(member.id!!, paymentResponse, option, 1)
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
