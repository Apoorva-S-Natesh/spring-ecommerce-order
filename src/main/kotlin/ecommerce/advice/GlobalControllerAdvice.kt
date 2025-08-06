package ecommerce.advice

import ecommerce.exception.AuthenticationException
import ecommerce.exception.AuthorizationException
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.ErrorResponse
import ecommerce.exception.InsufficientProductOptionsException
import ecommerce.exception.NotFoundException
import ecommerce.exception.ProductValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse {
        return ErrorResponse(
            error = e.errorCode,
            message = e.message!!,
        )
    }

    @ExceptionHandler(ProductValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleProductValidationException(e: ProductValidationException): ErrorResponse {
        return ErrorResponse(
            error = e.errorCode,
            message = e.message!!,
            fieldErrors = e.errors,
        )
    }

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleAuthenticationException(e: AuthenticationException): ErrorResponse {
        return ErrorResponse(
            error = e.errorCode,
            message = e.message!!,
        )
    }

    @ExceptionHandler(AuthorizationException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAuthorizationException(e: AuthorizationException): ErrorResponse {
        return ErrorResponse(
            error = e.errorCode,
            message = e.message!!,
        )
    }

    @ExceptionHandler(DuplicateNameException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateNameException(e: DuplicateNameException): ErrorResponse {
        return ErrorResponse(
            error = e.errorCode,
            message = e.message!!,
        )
    }

    @ExceptionHandler(InsufficientProductOptionsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: InsufficientProductOptionsException): ErrorResponse {
        return ErrorResponse(
            error = e.errorCode,
            message = e.message!!,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ErrorResponse {
        return ErrorResponse(
            error = "BAD_REQUEST",
            message = e.message!!,
        )
    }
}
