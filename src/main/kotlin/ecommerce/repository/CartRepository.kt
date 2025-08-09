package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CartRepository : JpaRepository<Cart, Long> {
    fun findByMemberId(memberId: Long): Cart?

    @Query("SELECT c FROM Cart c JOIN c.cartItem ci WHERE c.member.id = :memberId AND ci.productOption.id = :productOptionId")
    fun findByMemberIdAndCartItemProductOptionId(
        memberId: Long,
        productOptionId: Long,
    ): Cart?

    @Modifying
    fun deleteByMemberId(memberId: Long)
}
