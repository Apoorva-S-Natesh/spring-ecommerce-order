package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.member.id = :memberId")
    fun findByMemberId(memberId: Long): Cart?

    @Query("SELECT c FROM Cart c WHERE c.id = :cartId AND c.member.id = :memberId")
    fun findByIdAndMemberId(
        cartId: Long,
        memberId: Long,
    ): Cart?

    @Query("SELECT c FROM Cart c JOIN c.cartItem ci WHERE c.member.id = :memberId AND ci.productOption.id = :productOptionId")
    fun findByMemberIdAndCartItemProductOptionId(
        memberId: Long,
        productOptionId: Long,
    ): Cart?

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.member.id = :memberId")
    fun deleteByMemberId(memberId: Long)
}
