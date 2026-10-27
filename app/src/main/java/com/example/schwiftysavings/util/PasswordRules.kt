package com.example.schwiftysavings.util

/**
 * Registration password rules: at least 8 characters, with letters and a special character.
 * Extra variety (a number, mixed case, or a longer password) raises the strength bar.
 */
object PasswordRules {
    const val MIN_LENGTH = 8

    enum class Strength {
        EMPTY,
        WEAK,
        MEDIUM,
        STRONG
    }

    fun hasLetter(password: String): Boolean = password.any { it.isLetter() }

    fun hasSpecial(password: String): Boolean =
        password.any { !it.isLetterOrDigit() && !it.isWhitespace() }

    fun meetsRequirements(password: String): Boolean =
        password.length >= MIN_LENGTH && hasLetter(password) && hasSpecial(password)

    /** Null when the password can be used to register. */
    fun requirementError(password: String): String? = when {
        password.length < MIN_LENGTH -> "Password must be at least 8 characters"
        !hasLetter(password) -> "Password must include letters"
        !hasSpecial(password) -> "Password must include a special character"
        else -> null
    }

    fun strength(password: String): Strength {
        if (password.isEmpty()) return Strength.EMPTY
        if (!meetsRequirements(password)) return Strength.WEAK
        val extras = listOf(
            password.any { it.isDigit() },
            password.any { it.isUpperCase() } && password.any { it.isLowerCase() },
            password.length >= 12
        ).count { it }
        return if (extras == 0) Strength.MEDIUM else Strength.STRONG
    }

    fun fraction(password: String): Float = when (strength(password)) {
        Strength.EMPTY -> 0f
        Strength.WEAK -> {
            val steps = listOf(
                password.length >= MIN_LENGTH,
                hasLetter(password),
                hasSpecial(password)
            ).count { it }
            (steps.coerceAtLeast(1) / 3f) * 0.45f
        }
        Strength.MEDIUM -> 0.7f
        Strength.STRONG -> 1f
    }

    fun label(password: String): String = when (strength(password)) {
        Strength.EMPTY -> ""
        Strength.WEAK -> "Weak"
        Strength.MEDIUM -> "Medium"
        Strength.STRONG -> "Strong"
    }
}
