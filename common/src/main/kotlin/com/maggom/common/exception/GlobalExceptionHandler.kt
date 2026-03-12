package com.maggom.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(ex: MethodArgumentNotValidException): ErrorResponse {
        val message = ex.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage
            ?: "잘못된 요청입니다."

        return ErrorResponse(message)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ErrorResponse {
        log.debug("Bad request: {}", ex.message)

        return ErrorResponse(ex.message ?: "잘못된 요청입니다.")
    }

    @ExceptionHandler(TooManyRequestsException::class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun handleTooManyRequestsException(ex: TooManyRequestsException): ErrorResponse {
        log.debug("Too many requests: {}", ex.message)

        return ErrorResponse(ex.message ?: "너무 많은 요청입니다.")
    }

    @ExceptionHandler(MemberNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleMemberNotFoundException(ex: MemberNotFoundException): ErrorResponse {
        log.debug("Member not found: {}", ex.message)

        return ErrorResponse(ex.message ?: "존재하지 않는 회원입니다.")
    }

    @ExceptionHandler(MemberAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleMemberAlreadyExistsException(ex: MemberAlreadyExistsException): ErrorResponse {
        log.debug("Member already exists: {}", ex.message)

        return ErrorResponse(ex.message ?: "이미 가입된 회원입니다.")
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(ex: Exception): ErrorResponse {
        log.error("Unhandled exception", ex)

        return ErrorResponse("서버 오류가 발생했습니다.")
    }
}
