package ecommerce.exception

class AuthorizationException(message: String = "Access denied") : Exceptions("Forbidden", message)
