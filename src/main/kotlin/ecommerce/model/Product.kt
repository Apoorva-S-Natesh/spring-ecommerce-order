package ecommerce.model

data class Product(
    val id: Long? = null,
    val name: String,
    val price: Double,
    val imageUrl: String,
)
