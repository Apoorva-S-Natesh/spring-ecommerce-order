package ecommerce.exception

abstract class Exceptions(
    val errorCode: String,
    message: String,
) : RuntimeException(message)
