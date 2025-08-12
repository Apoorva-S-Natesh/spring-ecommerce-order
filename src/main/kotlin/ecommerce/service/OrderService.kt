package ecommerce.service

import ecommerce.dto.OrderResponse
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PaymentResponse
import ecommerce.dto.PlaceOrderRequest
import ecommerce.exception.OrderProcessingException
import ecommerce.exception.StripePaymentException
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.Payment
import ecommerce.model.ProductOption
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.ProductOptionRepository
import ecommerce.stripe.DeclineCode
import ecommerce.stripe.StripeClient
import ecommerce.utils.ResponseMapper.orderToResponse
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val cartItemRepository: CartItemRepository,
    private val memberRepository: MemberRepository,
    private val stripeClient: StripeClient,
) {
    private val restClient = RestClient.create()

    fun getMemberOrders(
        memberId: Long,
        page: Int,
        size: Int,
        sortBy: String,
    ): Page<OrderResponse> {
        validateMember(memberId)
        val pageable = PageRequest.of(page, size, Sort.by(sortBy))
        return orderRepository.findByMemberId(memberId, pageable).map { orderToResponse(it) }
    }

    fun getAllOrders(
        page: Int,
        size: Int,
        sortBy: String,
    ): Page<OrderResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(sortBy))
        return orderRepository.findAll(pageable).map { orderToResponse(it) }
    }

    fun getOrderDetails(orderId: Long): OrderResponse {
        val order =
            orderRepository.findByIdOrNull(orderId) ?: throw EntityNotFoundException("Order with id $orderId not found")
        return orderToResponse(order)
    }

    @Transactional
    fun placeOrder(
        req: PlaceOrderRequest,
        memberId: Long,
    ): OrderResponse {
        val productOption = validateProductOption(req.productOptionId)
        val amount = calculateAmount(productOption, req.quantity)
        val paymentRequest = buildPaymentRequest(req, amount)
        val paymentResponse = processPayment(paymentRequest)
        productOption.subtract(req.quantity)
        val orderResponse = createOrder(memberId, paymentResponse, productOption, req.quantity)
        removeCartItem(memberId, productOption.id!!)
        return orderResponse
    }

    private fun validateMember(memberId: Long): Member {
        return memberRepository.findByIdOrNull(memberId)
            ?: throw EntityNotFoundException("User with id $memberId not found")
    }

    private fun validateProductOption(productOptionId: Long): ProductOption {
        return productOptionRepository.findByIdOrNull(productOptionId)
            ?: throw EntityNotFoundException("Product Option not found")
    }

    private fun calculateAmount(
        productOption: ProductOption,
        quantity: Int,
    ): Long {
        return (productOption.product.price * quantity * 100).toLong()
    }

    private fun buildPaymentRequest(
        request: PlaceOrderRequest,
        amount: Long,
    ): PaymentRequest {
        return PaymentRequest(
            amount = amount,
            currency = request.currency,
            paymentMethod = request.paymentMethod,
        )
    }

    private fun processPayment(paymentRequest: PaymentRequest): PaymentResponse {
        try {
            return stripeClient.createCheckoutSession(paymentRequest)
        } catch (e: StripePaymentException) {
            val decline = DeclineCode.fromStripeCode(e.declineCode)
            throw OrderProcessingException(decline.stripeCode, decline.userMessage, e)
        }
    }

    fun createOrder(
        memberId: Long,
        paymentResponse: PaymentResponse,
        productOption: ProductOption,
        quantity: Int,
    ): OrderResponse {
        validateMember(memberId)
        val order =
            Order(
                memberId = memberId,
                orderDate = LocalDateTime.now(),
                orderItems = mutableListOf(),
            )
        val orderItem =
            OrderItem(
                quantity = quantity,
                price = productOption.product.price,
                productOption = productOption,
            )
        order.orderItems.add(orderItem)
        val payment =
            Payment(
                checkoutSessionId = paymentResponse.id,
                amount = paymentResponse.amount,
                currency = paymentResponse.currency,
                status = paymentResponse.status,
                paymentMethod = paymentResponse.paymentMethod,
            )
        order.payment = payment
        return orderToResponse(orderRepository.save(order))
    }

    private fun removeCartItem(
        memberId: Long,
        productOptionId: Long,
    ) {
        val member = validateMember(memberId)
        member.cart?.let { cart ->
            cartItemRepository.findByCartIdAndProductOptionId(cart.id!!, productOptionId)
                ?.let { cartItem ->
                    cart.updateQuantity(-cartItem.quantity)
                    cartItemRepository.delete(cartItem)
                }
        }
    }
}
