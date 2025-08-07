package ecommerce.service

import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.InsufficientProductOptionsException
import ecommerce.exception.NotFoundException
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.repository.ProductOptionRepository
import ecommerce.repository.ProductRepository
import ecommerce.util.toModel
import ecommerce.util.toResponse
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
) {
    fun findAllProducts(
        page: Int,
        size: Int,
        sortBy: String,
    ): Page<Product> {
        val pageable = PageRequest.of(page, size, Sort.by(sortBy))
        return productRepository.findAll(pageable)
    }

    fun findProductById(id: Long): Product {
        return productRepository.findByIdOrNull(id) ?: throw NotFoundException("Product with id $id not found")
    }

    @Transactional
    fun createProduct(request: ProductRequest): ProductResponse {
        if (productRepository.existsByName(request.name)) {
            throw DuplicateNameException("Product name already exists")
        }
        if (request.productOptions.isEmpty()) {
            throw InsufficientProductOptionsException("Product needs at least one option")
        }
        var product = request.toModel()
        val savedProduct = productRepository.save(product)

        request.productOptions.forEach { option ->
            option.productId = savedProduct.id!!
            productOptionRepository.save(ProductOption(option.name, option.quantity, product))
        }
        return savedProduct.toResponse()
    }

    @Transactional
    fun updateProduct(
        id: Long,
        request: ProductRequest,
    ): ProductResponse {
        if (!productRepository.existsById(id)) {
            throw NotFoundException("Product with id $id not found")
        }
        val updatedProduct = request.toModel(id)
        return productRepository.save(updatedProduct).toResponse()
    }

    fun deleteById(id: Long) {
        productOptionRepository.deleteProductOptionsByProductId(id)
        productRepository.deleteById(id)
    }
}
