package com.rodgalan.chatboot.users.domain

object PasswordPolicy {
    private const val MIN_LENGTH = 12
    private val UPPERCASE = Regex("[A-Z]")
    private val LOWERCASE = Regex("[a-z]")
    private val DIGIT = Regex("[0-9]")
    private val SPECIAL_CHARACTER = Regex("[^A-Za-z0-9]")

    fun validate(password: String) {
        val meetsPolicy = password.length >= MIN_LENGTH &&
            UPPERCASE.containsMatchIn(password) &&
            LOWERCASE.containsMatchIn(password) &&
            DIGIT.containsMatchIn(password) &&
            SPECIAL_CHARACTER.containsMatchIn(password)

        if (!meetsPolicy) {
            throw WeakPasswordError()
        }
    }
}
