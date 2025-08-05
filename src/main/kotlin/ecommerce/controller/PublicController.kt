package ecommerce.controller

import ecommerce.dto.member.LoginRequest
import ecommerce.dto.member.RegisterRequest
import ecommerce.dto.member.TokenResponse
import ecommerce.service.MemberService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/members")
@RestController
class PublicController(private val memberService: MemberService) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registerRequest: RegisterRequest,
    ): ResponseEntity<TokenResponse> {
        val token = memberService.register(registerRequest)
        val tokenResponse = TokenResponse(token = token)
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        val token = memberService.authenticate(loginRequest.email, loginRequest.password)
        val tokenResponse = TokenResponse(token = token)
        return ResponseEntity.ok(tokenResponse)
    }
}

@RestController
class TestEndpointsController {
    @GetMapping("/admin")
    fun adminEndpoint(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("message" to "Admin access granted"))
    }

    @GetMapping("/api/cart-items")
    fun cartItemsEndpoint(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("message" to "Cart access granted"))
    }
}
