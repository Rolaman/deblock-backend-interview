package org.deblock.exercise.controller

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.map { violation ->
            val propertyPath = violation.propertyPath.toString()
            val fieldName = propertyPath.substringAfterLast('.')
            "$fieldName: ${violation.message}"
        }
        
        val errorResponse = ErrorResponse(
            error = "Validation Failed",
            message = "Invalid request parameters: $errors",
        )
        
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(ex: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = "Missing Parameter",
            message = "Required parameter '${ex.parameterName}' of type '${ex.parameterType}' is missing",
        )
        
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = "Bad Request",
            message = ex.message ?: "Invalid argument",
        )
        
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = "Type Mismatch",
            message = "Invalid parameter type for '${ex.name}': expected ${ex.requiredType?.simpleName}",
        )
        
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = "Internal Server Error",
            message = "An unexpected error occurred",
        )
        
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
)
