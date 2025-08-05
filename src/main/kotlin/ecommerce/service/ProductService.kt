package ecommerce.service

import ecommerce.dto.product.ProductRequest
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.NotFoundException
import ecommerce.model.Product
import ecommerce.repository.ProductOptionRepository
import ecommerce.repository.ProductRepository
import ecommerce.util.toModel
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
    fun createProduct(request: ProductRequest): Product {
        if (productRepository.existsByName(request.name)) {
            throw DuplicateNameException("Product name already exists")
        }
        val product = request.toModel()
        return productRepository.save(product)
    }

    @Transactional
    fun updateProduct(
        id: Long,
        request: ProductRequest,
    ): Product {
        if (!productRepository.existsById(id)) {
            throw NotFoundException("Product with id $id not found")
        }
        val updatedProduct = request.toModel(id)
        return productRepository.save(updatedProduct)
    }

    fun deleteById(id: Long) {
        productOptionRepository.deleteProductOptionsByProductId(id)
        productRepository.deleteById(id)
    }
}
