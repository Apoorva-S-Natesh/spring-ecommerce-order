package ecommerce.exception

class InsufficientProductOptionsException(message: String = "Invalid request") : Exceptions("BAD_REQUEST", message)
