package com.rodgalan.chatboot.users.infrastructure.http

import com.rodgalan.chatboot.users.domain.EmailAlreadyRegisteredError
import com.rodgalan.chatboot.users.domain.InvalidEmailFormatError
import com.rodgalan.chatboot.users.domain.UserAlreadyActiveError
import com.rodgalan.chatboot.users.domain.VerificationTokenAlreadyConsumedError
import com.rodgalan.chatboot.users.domain.VerificationTokenExpiredError
import com.rodgalan.chatboot.users.domain.VerificationTokenNotFoundError
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

    @ExceptionHandler(VerificationTokenNotFoundError::class)
    fun onVerificationTokenNotFound(error: VerificationTokenNotFoundError): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @ExceptionHandler(
        VerificationTokenExpiredError::class,
        VerificationTokenAlreadyConsumedError::class,
        UserAlreadyActiveError::class,
    )
    fun onVerificationTokenGone(error: RuntimeException): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.GONE).build()

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onUnreadableBody(error: HttpMessageNotReadableException): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
}
