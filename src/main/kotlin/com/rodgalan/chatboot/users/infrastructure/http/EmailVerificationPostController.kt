package com.rodgalan.chatboot.users.infrastructure.http

import com.rodgalan.chatboot.users.application.VerifyUserEmail
import com.rodgalan.chatboot.users.application.VerifyUserEmailCommand
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/email-verifications")
class EmailVerificationPostController(private val verifyUserEmail: VerifyUserEmail) {

    @PostMapping
    fun verify(@RequestBody request: EmailVerificationRequest): ResponseEntity<Void> {
        verifyUserEmail.verify(VerifyUserEmailCommand(token = request.token))
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
