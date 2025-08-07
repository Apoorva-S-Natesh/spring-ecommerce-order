package ecommerce.repository

import ecommerce.model.ProductOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface ProductOptionRepository : JpaRepository<ProductOption, Long> {
    fun existsByName(name: String): Boolean

    @Modifying
    fun deleteByProductId(productId: Long)
}
