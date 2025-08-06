package ecommerce.service

import ecommerce.dto.ProductOptionRequest
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.NotFoundException
import ecommerce.model.ProductOption
import ecommerce.repository.ProductOptionRepository
import ecommerce.repository.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ProductOptionService(
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
) {
    @Transactional
    fun saveProductOption(
        productId: Long,
        request: ProductOptionRequest,
        productOptionId: Long? = null,
    ): ProductOption {
        if (!productRepository.existsById(productId)) {
            throw NotFoundException("Product does not exist")
        }

        val product = productRepository.findById(productId).get()
        val existingOption = productOptionId?.let { productOptionRepository.findById(it).orElse(null) }

        if (existingOption != null) {
            val updatedOption =
                ProductOption(
                    id = productOptionId,
                    name = request.name,
                    quantity = request.quantity,
                    product = product,
                )
            return productOptionRepository.save(updatedOption)
        } else {
            if (productOptionRepository.existsByName(request.name)) {
                throw DuplicateNameException("Product option name in this product already exists")
            }
            val newOption =
                ProductOption(
                    name = request.name,
                    quantity = request.quantity,
                    product = product,
                )
            return productOptionRepository.save(newOption)
        }
    }

    @Transactional
    fun removeProductOption(id: Long) {
        if (!productOptionRepository.existsById(id)) {
            throw NotFoundException("Product option name does not exists")
        }
        productOptionRepository.deleteById(id)
    }
}
