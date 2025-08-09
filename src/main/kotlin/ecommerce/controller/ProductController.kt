package ecommerce.controller

import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.model.Product
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RequestMapping("/api/products")
@RestController
class ProductController(private val productService: ProductService) {
    @GetMapping()
    fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
    ): Page<Product> = productService.findAllProducts(page, size, sortBy)

    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long,
    ): Product = productService.findProductById(id)

    @PostMapping()
    fun createProduct(
        @Valid @RequestBody productRequest: ProductRequest,
    ): ResponseEntity<ProductResponse> {
        val saved = productService.createProduct(productRequest)
        return ResponseEntity.created(URI.create("/api/products/${saved.id}")).body(saved)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @Valid @RequestBody productRequest: ProductRequest,
        @PathVariable id: Long,
    ): ProductResponse {
        return productService.updateProduct(id, productRequest)
    }

    @PatchMapping("/{id}")
    fun updateProductPartially(
        @Valid @RequestBody productRequest: ProductRequest,
        @PathVariable id: Long,
    ): ProductResponse {
        return productService.updateProduct(id, productRequest)
    }

    @DeleteMapping("/{id}")
    fun deleteProductById(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        productService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
