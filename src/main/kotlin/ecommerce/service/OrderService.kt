package ecommerce.service

import ecommerce.dto.OrderResponse
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PaymentResponse
import ecommerce.dto.PlaceOrderRequest
import ecommerce.model.Currency
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderSortOption
import ecommerce.model.Payment
import ecommerce.model.PaymentStatus
import ecommerce.model.ProductOption
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.ProductOptionRepository
import ecommerce.utils.ResponseMapper.orderToResponse
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val cartItemRepository: CartItemRepository,
    private val memberRepository: MemberRepository,
    private val paymentService: PaymentService,
) {
    fun getMemberOrders(
        memberId: Long,
        page: Int,
        size: Int,
        sortBy: OrderSortOption,
    ): Page<OrderResponse> {
        validateMember(memberId)
        val pageable = PageRequest.of(page, size, Sort.by(sortBy.fieldName))
        return orderRepository.findByMemberId(memberId, pageable).map { orderToResponse(it) }
    }

    fun getAllOrders(
        page: Int,
        size: Int,
        sortBy: OrderSortOption,
    ): Page<OrderResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(sortBy.fieldName))
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
        val amount = productOption.calculateAmount(req.quantity)
        val paymentRequest = buildPaymentRequest(req, amount)
        val paymentResponse = paymentService.processPayment(paymentRequest)
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

    fun createOrder(
        memberId: Long,
        paymentResponse: PaymentResponse,
        productOption: ProductOption,
        quantity: Int,
    ): OrderResponse {
        validateMember(memberId)
        val orderItem =
            OrderItem(
                quantity = quantity,
                price = productOption.product.price,
                productOption = productOption,
            )
        val payment =
            Payment(
                checkoutSessionId = paymentResponse.id,
                amount = paymentResponse.amount,
                currency = Currency.fromCode(paymentResponse.currency),
                status = PaymentStatus.fromStripeStatus(paymentResponse.status),
                paymentMethod = paymentResponse.paymentMethod,
            )
        val order =
            Order(
                memberId = memberId,
                orderDate = LocalDateTime.now(),
                orderItems = mutableListOf(),
                payment = payment,
            )
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
