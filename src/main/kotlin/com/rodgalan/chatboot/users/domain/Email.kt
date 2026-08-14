package com.rodgalan.chatboot.users.domain

@JvmInline
value class Email private constructor(val value: String) {
    companion object {
        private val FORMAT = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun of(value: String): Email {
            if (!FORMAT.matches(value)) {
                throw InvalidEmailFormatError(value)
            }
            return Email(value)
        }
    }
}
