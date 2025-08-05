package ecommerce.model

import jakarta.persistence.*

@Entity
@Table(name = "products")
class Product(
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "price", nullable = false)
    var price: Double,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "imageUrl", nullable = false)
    var imageUrl: String,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    init {
        require(name.isNotEmpty() && name.length <= MAX_NAME_LENGTH)
        require(name.all { it.isLetterOrDigit() || it in ALLOWED_SPECIAL_CHAR }) { "Name contains invalid characters" }
        require(imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))
    }

    companion object {
        private const val MAX_NAME_LENGTH = 15
        private const val ALLOWED_SPECIAL_CHAR = "()[]+-&/_ "
        private const val URL_PATTERN1 = "^http://"
        private const val URL_PATTERN2 = "^https://"
    }

    override fun toString(): String {
        return "Product(id=$id, name=$name, price=$price quantity=$quantity)"
    }
}
