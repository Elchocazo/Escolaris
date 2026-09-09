package com.example.domain.model

/**
 * Modelo de datos para el sistema de insignias (medallas) estilo Pokémon GO.
 * Soporta seguimiento de progreso numérico hacia metas y objetivos escolares.
 */
data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "ACADEMIC", "PARENT", "ATTENDANCE", "STEAM"
    val currentProgress: Int,
    val targetProgress: Int,
    val iconResId: Int = 0,
    val iconUrl: String? = null,
    val emoji: String = "🎖️",
    val unlockedAtDate: String? = null,
    val xpReward: Int = 150,
    val creditReward: Int = 75
) {
    /**
     * Propiedad calculada: verdadero si el progreso actual alcanza o supera la meta.
     */
    val isUnlocked: Boolean
        get() = currentProgress >= targetProgress

    /**
     * Fracción de progreso entre 0.0f y 1.0f.
     */
    val progressFraction: Float
        get() = if (targetProgress > 0) {
            (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
        } else 0f

    /**
     * Porcentaje de avance (0% a 100%).
     */
    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}
