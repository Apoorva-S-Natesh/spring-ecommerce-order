package ecommerce.service

import ecommerce.dto.product.ProductPatchRequest
import ecommerce.dto.product.ProductRequest
import ecommerce.exception.ProductValidationException
import ecommerce.model.Product
import ecommerce.repository.ProductRepository
import ecommerce.validation.NAME_LENGTH_MAXIMUM
import ecommerce.validation.PRODUCT_NAME_PATTERN
import ecommerce.validation.PRODUCT_PRICE_MINIMUM
import ecommerce.validation.URL_PATTERN
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    private fun validateBasicProductData(request: ProductRequest) {
        val errors = mutableListOf<String>()

        if (request.name.isBlank()) {
            errors.add("Product name cannot be blank")
        }
        if (request.name.length > NAME_LENGTH_MAXIMUM) {
            errors.add("Product name must be shorter than $NAME_LENGTH_MAXIMUM characters")
        }
        if (!request.name.matches(PRODUCT_NAME_PATTERN.toRegex())) {
            errors.add("Product name contains invalid characters")
        }

        val minPrice = PRODUCT_PRICE_MINIMUM.toDouble()
        if (request.price <= minPrice) {
            errors.add("Product price must be greater than $minPrice")
        }

        if (request.imageUrl.isBlank()) {
            errors.add("Product image URL cannot be blank")
        }
        if (!request.imageUrl.matches(URL_PATTERN.toRegex())) {
            errors.add("Product image URL must start with http:// or https://")
        }

        if (errors.isNotEmpty()) {
            throw ProductValidationException(errors)
        }
    }

    private fun validateProductNameUniqueness(
        name: String,
        excludeId: Long? = null,
    ) {
        if (excludeId != null) {
            val existingProduct = productRepository.findById(excludeId)
            if (existingProduct.name != name && productRepository.existsByName(name)) {
                throw ProductValidationException("Product name already exists")
            }
        } else {
            if (productRepository.existsByName(name)) {
                throw ProductValidationException("Product name already exists")
            }
        }
    }

    private fun validateProductForCreation(request: ProductRequest) {
        validateBasicProductData(request)
        validateProductNameUniqueness(request.name)
    }

    private fun validateProductForUpdate(
        id: Long,
        request: ProductRequest,
    ) {
        validateBasicProductData(request)

        val existingProduct = productRepository.findById(id)
        if (existingProduct.name != request.name) {
            validateProductNameUniqueness(request.name, id)
        }
    }

    fun createProduct(request: ProductRequest): Product {
        validateProductForCreation(request)

        val product =
            Product(
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
            )
        return productRepository.save(product)
    }

    fun updateProduct(
        id: Long,
        request: ProductRequest,
    ): Product {
        validateProductForUpdate(id, request)

        val product =
            Product(
                id = id,
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
            )
        return productRepository.update(id, product)
    }

    fun patchProduct(
        id: Long,
        request: ProductPatchRequest,
    ): Product {
        request.name?.let { newName ->
            validateProductNameUniqueness(newName, id)
        }

        return productRepository.patch(id, request)
    }

    fun findAll(): List<Product> = productRepository.findAll()

    fun findById(id: Long): Product = productRepository.findById(id)

    fun deleteById(id: Long) = productRepository.delete(id)
}
