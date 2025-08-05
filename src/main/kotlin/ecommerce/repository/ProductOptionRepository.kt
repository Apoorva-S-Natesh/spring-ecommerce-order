package ecommerce.repository

import ecommerce.model.ProductOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductOptionRepository : JpaRepository<ProductOption, Long> {
    fun existsByName(name: String): Boolean

    @Modifying
    @Query("DELETE FROM ProductOption po WHERE po.product.id = :productId")
    fun deleteProductOptionsByProductId(productId: Long)
}
