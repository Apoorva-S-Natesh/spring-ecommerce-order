package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.member.id = :memberId")
    fun findByMember_Id(memberId: Long): Cart?

    @Query("SELECT c FROM Cart c WHERE c.id = :cartId AND c.member.id = :memberId")
    fun findByIdAndMember_Id(
        cartId: Long,
        memberId: Long,
    ): Cart?

    @Query("SELECT c FROM Cart c JOIN c.cartItem ci WHERE c.member.id = :memberId AND ci.productOption.id = :productOptionId")
    fun findByMember_IdAndCartItemProductOptionId(
        memberId: Long,
        productOptionId: Long,
    ): Cart?

    @Query(
        "DELETE FROM Cart c WHERE c.member.id = :memberId AND EXISTS (SELECT ci FROM CartItem ci WHERE ci.cart = c AND ci.productOption.id = :productOptionId)",
    )
    fun deleteByMember_IdAndCartItemProductOptionId(
        memberId: Long,
        productOptionId: Long,
    )

    @Query("DELETE FROM Cart c WHERE c.member.id = :memberId")
    fun deleteByMember_Id(memberId: Long)
}
