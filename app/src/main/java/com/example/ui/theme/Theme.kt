package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppColorTheme(val displayName: String, val primaryColor: Color) {
    ROYAL_BLUE("Azul Colegial (Niños)", Color(0xFF1D4ED8)),
    MAGENTA_PINK("Rosa Magenta (Niñas)", Color(0xFFE11D74)),
    EMERALD("Esmeralda", Color(0xFF047857)),
    PURPLE("Púrpura Creativo", Color(0xFF6D28D9)),
    AMBER("Ámbar Solar", Color(0xFFB45309))
}

enum class DarkThemeMode(val displayName: String) {
    LIGHT("Modo Claro"),
    DARK("Modo Oscuro")
}

private fun getLightScheme(theme: AppColorTheme): ColorScheme {
    val (primary, primaryContainer, onPrimaryContainer) = when (theme) {
        AppColorTheme.ROYAL_BLUE -> Triple(BluePrimaryLight, BluePrimaryContainerLight, BlueOnPrimaryContainerLight)
        AppColorTheme.MAGENTA_PINK -> Triple(PinkPrimaryLight, PinkPrimaryContainerLight, PinkOnPrimaryContainerLight)
        AppColorTheme.EMERALD -> Triple(EmeraldPrimaryLight, Color(0xFFD1FAE5), Color(0xFF065F46))
        AppColorTheme.PURPLE -> Triple(PurpleThemePrimaryLight, Color(0xFFEDE9FE), Color(0xFF4C1D95))
        AppColorTheme.AMBER -> Triple(AmberPrimaryLight, Color(0xFFFEF3C7), Color(0xFF78350F))
    }
    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = if (theme == AppColorTheme.MAGENTA_PINK) PinkPrimaryLight else BlueSecondaryLight,
        onSecondary = Color.White,
        tertiary = BlueTertiaryLight,
        background = BlueBackgroundLight,
        surface = BlueSurfaceLight,
        surfaceVariant = BlueSurfaceVariantLight,
        outline = BlueOutlineLight
    )
}

private fun getDarkScheme(theme: AppColorTheme): ColorScheme {
    val (primary, primaryContainer, onPrimaryContainer) = when (theme) {
        AppColorTheme.ROYAL_BLUE -> Triple(BluePrimaryDark, BluePrimaryContainerDark, BlueOnPrimaryContainerDark)
        AppColorTheme.MAGENTA_PINK -> Triple(PinkPrimaryDark, PinkPrimaryContainerDark, PinkOnPrimaryContainerDark)
        AppColorTheme.EMERALD -> Triple(EmeraldPrimaryDark, Color(0xFF064E3B), Color(0xFFA7F3D0))
        AppColorTheme.PURPLE -> Triple(PurpleThemePrimaryDark, Color(0xFF4C1D95), Color(0xFFDDD6FE))
        AppColorTheme.AMBER -> Triple(AmberPrimaryDark, Color(0xFF78350F), Color(0xFFFDE68A))
    }
    return darkColorScheme(
        primary = primary,
        onPrimary = Color(0xFF0F172A),
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = if (theme == AppColorTheme.MAGENTA_PINK) PinkPrimaryDark else BlueSecondaryDark,
        onSecondary = Color(0xFF0F172A),
        tertiary = BlueTertiaryDark,
        background = BlueBackgroundDark,
        surface = BlueSurfaceDark,
        surfaceVariant = BlueSurfaceVariantDark,
        outline = BlueOutlineDark
    )
}

@Composable
fun EscolarisTheme(
    themeMode: DarkThemeMode = DarkThemeMode.LIGHT,
    colorTheme: AppColorTheme = AppColorTheme.ROYAL_BLUE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        DarkThemeMode.LIGHT -> false
        DarkThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) getDarkScheme(colorTheme) else getLightScheme(colorTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
