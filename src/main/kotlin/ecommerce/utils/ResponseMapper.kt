package ecommerce.utils

import ecommerce.dto.OrderItemResponse
import ecommerce.dto.OrderResponse
import ecommerce.dto.ProductOptionResponse
import ecommerce.dto.ProductResponse
import ecommerce.dto.cart.CartResponse
import ecommerce.dto.cartItem.CartItemResponse
import ecommerce.dto.member.MemberResponse
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.PaymentStatus
import ecommerce.model.Product
import ecommerce.model.ProductOption

object ResponseMapper {
    fun cartToResponse(cart: Cart) =
        CartResponse(
            id = cart.id ?: throw IllegalStateException("Cart ID cannot be null"),
            quantity = cart.quantity,
            newItemAddedAt = cart.newItemAddedAt,
            memberId = cart.member?.id,
            cartItems = cart.cartItem.map { cartItemToResponse(it) },
        )

    fun memberToResponse(member: Member) =
        MemberResponse(
            id = member.id ?: throw IllegalStateException("Member ID cannot be null"),
            email = member.email,
            name = member.name,
            cartId = member.cart?.id,
        )

    fun cartItemToResponse(cartItem: CartItem) =
        CartItemResponse(
            cartItem.id,
            cartItem.cart.id,
            productOptionToResponse(cartItem.productOption),
            cartItem.quantity,
        )

    fun productToResponse(product: Product) = ProductResponse(product.id, product.name, product.quantity, product.price, product.imageUrl)

    fun productOptionToResponse(productOption: ProductOption) =
        ProductOptionResponse(
            productOption.id,
            productOption.name,
            productOption.product.id,
        )

    fun orderToResponse(order: Order) =
        OrderResponse(
            id = order.id,
            orderDate = order.orderDate,
            status = order.payment?.status ?: PaymentStatus.UNKNOWN,
            amount = order.payment?.amount,
            orderItems =
                order.orderItems.map { item ->
                    OrderItemResponse(
                        item.quantity,
                        item.price,
                        item.productOption.id!!,
                    )
                },
        )
}
