package com.rodgalan.chatboot.users.infrastructure.http

import com.rodgalan.chatboot.users.domain.EmailAlreadyRegisteredError
import com.rodgalan.chatboot.users.domain.InvalidEmailFormatError
import com.rodgalan.chatboot.users.domain.WeakPasswordError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class UsersApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredError::class)
    fun onEmailAlreadyRegistered(error: EmailAlreadyRegisteredError): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.CONFLICT).build()

    @ExceptionHandler(InvalidEmailFormatError::class, WeakPasswordError::class)
    fun onInvalidRegistrationData(error: RuntimeException): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build()

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onUnreadableBody(error: HttpMessageNotReadableException): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
}
