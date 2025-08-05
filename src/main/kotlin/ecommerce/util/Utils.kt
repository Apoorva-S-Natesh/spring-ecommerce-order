package ecommerce.util

import ecommerce.dto.member.RegisterRequest
import ecommerce.dto.product.ProductRequest
import ecommerce.model.Member
import ecommerce.model.Product
import ecommerce.model.ProductOption

fun ProductRequest.toModel(id: Long? = null) = Product(name, price, quantity, imageUrl, id)

fun RegisterRequest.toModel(
    id: Long,
    hashedPassword: String,
) = Member(email, hashedPassword, name, role)

data class ProductResponse(
    val id: Long?,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String,
    val productOptions: List<ProductOptionResponse> = emptyList(),
)

data class ProductOptionResponse(
    val id: Long?,
    val name: String,
    val quantity: Int,
)

fun Product.toResponse() =
    ProductResponse(
        id = id,
        name = name,
        price = price,
        quantity = quantity,
        imageUrl = imageUrl,
    )

fun ProductOption.toResponse() =
    ProductOptionResponse(
        id = id,
        name = name,
        quantity = quantity,
    )
