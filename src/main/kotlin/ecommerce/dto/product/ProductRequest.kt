package ecommerce.dto.product

data class ProductRequest(
    val name: String,
    val price: Double,
    val imageUrl: String,
)
