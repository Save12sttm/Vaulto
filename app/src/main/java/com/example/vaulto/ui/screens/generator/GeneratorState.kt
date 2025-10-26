package com.example.vaulto.ui.screens.generator

data class GeneratorState(
    val generatedPassword: String = "",
    val length: Int = 16,
    val useUppercase: Boolean = true,
    val useLowercase: Boolean = true,
    val useNumbers: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.GOOD,
    val entropy: Double = 0.0,
    val generationHistory: List<String> = emptyList()
)

enum class PasswordStrength(val displayName: String, val minEntropy: Double) {
    VERY_WEAK("Very Weak", 0.0),
    WEAK("Weak", 28.0),
    FAIR("Fair", 36.0),
    GOOD("Good", 60.0),
    STRONG("Strong", 80.0),
    VERY_STRONG("Very Strong", 100.0);

    companion object {
        fun fromEntropy(entropy: Double): PasswordStrength {
            return when {
                entropy >= VERY_STRONG.minEntropy -> VERY_STRONG
                entropy >= STRONG.minEntropy -> STRONG
                entropy >= GOOD.minEntropy -> GOOD
                entropy >= FAIR.minEntropy -> FAIR
                entropy >= WEAK.minEntropy -> WEAK
                else -> VERY_WEAK
            }
        }
    }
}