package com.example.offers.api

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBodyValidation(exception: MethodArgumentNotValidException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request validation failed",
        )

        problem.title = "Validation error"
        problem.setProperty(
            "errors",
            exception.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") },
        )

        return problem
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleParameterValidation(exception: ConstraintViolationException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request parameter validation failed",
        )

        problem.title = "Validation error"
        problem.setProperty(
            "errors",
            exception.constraintViolations.associate {
                it.propertyPath.toString() to it.message
            },
        )

        return problem
    }
}
