package com.example.domain.validation

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
}

object ValidationUtils {

    const val MIN_GRADE = 1.0
    const val MAX_GRADE = 5.0
    const val PASSING_GRADE = 3.0

    /**
     * Validates an academic grade to ensure it lies strictly within the 1.0 to 5.0 scale.
     */
    fun validateGrade(grade: Double): ValidationResult {
        return if (grade in MIN_GRADE..MAX_GRADE) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("La calificación debe estar entre $MIN_GRADE y $MAX_GRADE.")
        }
    }

    /**
     * Clamps a grade safely into 1.0 to 5.0 with 1 decimal precision.
     */
    fun sanitizeGrade(grade: Double): Double {
        val clamped = grade.coerceIn(MIN_GRADE, MAX_GRADE)
        return (Math.round(clamped * 10.0) / 10.0)
    }

    /**
     * Validates non-empty text inputs with length constraints.
     */
    fun validateNonEmptyText(text: String, fieldName: String, minLength: Int = 2, maxLength: Int = 200): ValidationResult {
        val trimmed = text.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("El campo $fieldName no puede estar vacío.")
            trimmed.length < minLength -> ValidationResult.Invalid("El campo $fieldName debe tener al menos $minLength caracteres.")
            trimmed.length > maxLength -> ValidationResult.Invalid("El campo $fieldName no puede exceder $maxLength caracteres.")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates time strings in 24h format (HH:mm)
     */
    fun validateTimeFormat(timeStr: String): ValidationResult {
        val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        return if (regex.matches(timeStr.trim())) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Formato de hora inválido. Usa el formato HH:mm (ej. 07:30).")
        }
    }

    /**
     * Validates credit cost
     */
    fun validateCreditCost(cost: Int): ValidationResult {
        return if (cost in 1..10000) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("El costo en créditos debe ser mayor a 0 y menor a 10,000.")
        }
    }

    /**
     * Formats proper names and surnames so every word starts with a capital letter.
     * e.g., "juan camilo perez rojas" -> "Juan Camilo Perez Rojas"
     * Handles prefixes, accents, and compound names properly.
     */
    fun formatProperNoun(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                val firstLetterIdx = word.indexOfFirst { it.isLetter() }
                if (firstLetterIdx != -1) {
                    val prefix = word.substring(0, firstLetterIdx)
                    val char = word[firstLetterIdx]
                    val letter = if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString()
                    val rest = word.substring(firstLetterIdx + 1).lowercase()
                    prefix + letter + rest
                } else {
                    word
                }
            }
    }
}

