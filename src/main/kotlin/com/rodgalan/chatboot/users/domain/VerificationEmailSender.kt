package com.rodgalan.chatboot.users.domain

interface VerificationEmailSender {
    fun send(email: Email, token: VerificationToken)
}
