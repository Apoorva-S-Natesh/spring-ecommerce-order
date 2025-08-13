package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "product_options")
class ProductOption(
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    init {
        require(name.isNotBlank()) { "Name should not be empty" }
        require(name.length in 0..MAX_OPTION_NAME_LENGTH) { "Name should have length between 1 and 50" }
        require(name.all { it.isLetterOrDigit() || it in ALLOWED_SPECIAL_CHAR }) { "Name contains invalid characters" }
        require(quantity in MIN_QUANTITY..MAX_QUANTITY)
    }

    fun subtract(quantity: Int) {
        if (quantity < 1) throw IllegalArgumentException("Quantity must be greater than 0")
        if (quantity > this.quantity) throw IllegalArgumentException("Quantity can not be greater than stock of product quantity")
        this.quantity -= quantity
    }

//    fun updateQuantity(newQuantity: Int) {
//        quantity = newQuantity
//    }

    fun validateQuantity(requestedQuantity: Int) {
        if (requestedQuantity > quantity) {
            throw IllegalArgumentException(
                "Requested quantity ($requestedQuantity) exceeds available stock ($quantity)",
            )
        }
    }

    companion object {
        private const val MIN_QUANTITY = 1
        private const val MAX_QUANTITY = 99999999
        private const val MAX_OPTION_NAME_LENGTH = 50
        private const val ALLOWED_SPECIAL_CHAR = "()[]+-&/_ "
    }
}
