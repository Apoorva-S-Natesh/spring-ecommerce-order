package ecommerce.exception

class AuthenticationException(message: String = "Authentication failed") : Exceptions("UNAUTHORIZED", message)
