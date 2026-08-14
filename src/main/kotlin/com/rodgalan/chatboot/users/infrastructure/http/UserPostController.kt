package com.rodgalan.chatboot.users.infrastructure.http

import com.rodgalan.chatboot.users.application.RegisterUser
import com.rodgalan.chatboot.users.application.RegisterUserCommand
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserPostController(private val registerUser: RegisterUser) {

    @PostMapping
    fun register(@RequestBody request: RegisterUserRequest): ResponseEntity<Void> {
        registerUser.register(RegisterUserCommand(email = request.email, password = request.password))
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
