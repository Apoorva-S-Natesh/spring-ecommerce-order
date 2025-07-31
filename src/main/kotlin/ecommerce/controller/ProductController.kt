package ecommerce.controller

import ecommerce.dto.product.ProductPatchRequest
import ecommerce.dto.product.ProductRequest
import ecommerce.model.Product
import ecommerce.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RequestMapping("/api/products")
@RestController
class ProductController(private val productService: ProductService) {
    @GetMapping()
    fun getProducts(): List<Product> = productService.findAll()

    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long,
    ): Product = productService.findById(id)

    @PostMapping()
    fun createProduct(
        @RequestBody productRequest: ProductRequest,
    ): ResponseEntity<Product> {
        val saved = productService.createProduct(productRequest)
        return ResponseEntity.created(URI.create("/api/products/${saved.id}")).body(saved)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @RequestBody productRequest: ProductRequest,
        @PathVariable id: Long,
    ): Product {
        return productService.updateProduct(id, productRequest)
    }

    @PatchMapping("/{id}")
    fun updateProductPartially(
        @RequestBody productPatchRequest: ProductPatchRequest,
        @PathVariable id: Long,
    ): Product {
        return productService.patchProduct(id, productPatchRequest)
    }

    @DeleteMapping("/{id}")
    fun deleteProductById(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        productService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
