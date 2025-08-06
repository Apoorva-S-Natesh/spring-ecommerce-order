package ecommerce.controller

import ecommerce.dto.product.ProductOptionRequest
import ecommerce.dto.product.ProductRequest
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.service.ProductOptionService
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import java.net.URI
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/admin/products")
@RestController
class AdminController(private val productService: ProductService, private val productOptionService: ProductOptionService) {
    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long,
    ): Product = productService.findProductById(id)

    @GetMapping("")
    fun getAllProducts(
        @RequestParam(defaultValue = "0")page: Int,
        @RequestParam(defaultValue = "10")size: Int,
        @RequestParam(defaultValue = "name")sortBy: String,
    ): Page<Product> = productService.findAllProducts(page, size, sortBy)

    @PostMapping("")
    fun createProduct(
        @Valid @RequestBody productRequest: ProductRequest,
    ): ResponseEntity<Product> {
        val saved = productService.createProduct(productRequest)
        return ResponseEntity.created(URI.create("/api/products/${saved.id}")).body(saved)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @Valid @RequestBody productRequest: ProductRequest,
        @PathVariable id: Long,
    ): ResponseEntity<Product> {
        val updated = productService.updateProduct(id, productRequest)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteProductById(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        productService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("{productId}/options")
    fun addProductOption(
        @PathVariable productId: Long,
        @Valid @RequestBody productOptionRequest: ProductOptionRequest,
    ): ResponseEntity<ProductOption> {
        val saved = productOptionService.saveProductOption(productId, productOptionRequest)
        return ResponseEntity
            .created(URI.create("/$productId/options/${saved.id}"))
            .body(saved)
    }

    @PutMapping("{productId}/options/{optionId}")
    fun updateOption(
        @PathVariable productId: Long,
        @PathVariable optionId: Long,
        @RequestBody productOptionRequest: ProductOptionRequest,
    ): ResponseEntity<ProductOption> {
        val updated = productOptionService.saveProductOption(productId, productOptionRequest, optionId)
        return ResponseEntity
            .ok()
            .body(updated)
    }
}
