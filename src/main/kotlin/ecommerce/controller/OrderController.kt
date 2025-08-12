package ecommerce.controller

import ecommerce.dto.OrderResponse
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.exception.ErrorResponse
import ecommerce.exception.OrderProcessingException
import ecommerce.service.OrderService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {
    @PostMapping("")
    fun placeOrder(
        @Valid @RequestBody placeOrderRequest: PlaceOrderRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        return try {
            val order = orderService.placeOrder(placeOrderRequest, user.userId)
            ResponseEntity.ok(order)
        } catch (e: OrderProcessingException) {
            ResponseEntity.badRequest().body(ErrorResponse("BAD_REQUEST", e.message!!))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ErrorResponse("BAD_REQUEST", e.message ?: "Unknown error"))
        }
    }

    @GetMapping("")
    fun getMemberOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        user: AuthenticatedUser,
    ): ResponseEntity<Page<OrderResponse>> {
        val orders = orderService.getMemberOrders(user.userId, page, size, sortBy)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/admin")
    fun getAllOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        user: AuthenticatedUser,
    ): ResponseEntity<Page<OrderResponse>> {
        val orders = orderService.getAllOrders(page, size, sortBy)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{orderId}")
    fun getOrderDetails(
        @PathVariable orderId: Long,
    ): ResponseEntity<OrderResponse> {
        val order = orderService.getOrderDetails(orderId)
        return ResponseEntity.ok(order)
    }
}
