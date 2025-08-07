package ecommerce.util

import ecommerce.dto.ProductRequest
import ecommerce.dto.member.RegisterRequest
import ecommerce.model.Member
import ecommerce.model.Product

fun ProductRequest.toModel(id: Long? = null) = Product(name, price, quantity, imageUrl, id)

fun RegisterRequest.toModel(hashedPassword: String) = Member(email, hashedPassword, name, role)
