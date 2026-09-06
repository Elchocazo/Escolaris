package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class AuthThemePalette(
    val isPink: Boolean,
    val name: String,
    val primary: Color,
    val light: Color,
    val dark: Color,
    val gradientStart: Color,
    val gradientEnd: Color
)

// Magenta / Rosa Palette (Girls / Niñas)
val MagentaAuthPalette = AuthThemePalette(
    isPink = true,
    name = "Rosa",
    primary = Color(0xFFE11D74),
    light = Color(0xFFF43F8E),
    dark = Color(0xFFBE185D),
    gradientStart = Color(0xFFEC4899),
    gradientEnd = Color(0xFFDB2777)
)

// Electric Royal Blue Palette from uploaded image (Boys / Niños)
val BlueAuthPalette = AuthThemePalette(
    isPink = false,
    name = "Azul",
    primary = Color(0xFF2563EB),
    light = Color(0xFF60A5FA),
    dark = Color(0xFF1D4ED8),
    gradientStart = Color(0xFF3B82F6),
    gradientEnd = Color(0xFF1E40AF)
)

val AuthBackgroundOffWhite = Color(0xFFF9F9FC)
val AuthTextDark = Color(0xFF1E293B)
val AuthTextMuted = Color(0xFF64748B)

/**
 * Renders the top-right circular and organic curved bubble shapes
 */
@Composable
fun AuthTopBubblesBackground(
    modifier: Modifier = Modifier,
    palette: AuthThemePalette = MagentaAuthPalette
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Large Top-Right Outer Bubble
        val path1 = Path().apply {
            moveTo(width * 0.45f, 0f)
            cubicTo(
                width * 0.55f, height * 0.12f,
                width * 0.85f, height * 0.22f,
                width, height * 0.18f
            )
            lineTo(width, 0f)
            close()
        }

        drawPath(
            path = path1,
            brush = Brush.linearGradient(
                colors = listOf(palette.gradientStart, palette.primary),
                start = Offset(width * 0.5f, 0f),
                end = Offset(width, height * 0.2f)
            )
        )

        // Inner Top-Right Accent Circle Bubble
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.light.copy(alpha = 0.85f), palette.dark),
                center = Offset(width * 0.88f, height * 0.08f),
                radius = width * 0.28f
            ),
            radius = width * 0.28f,
            center = Offset(width * 0.88f, height * 0.08f)
        )
    }
}

/**
 * Renders the bottom organic wave shapes enclosing the action buttons
 */
@Composable
fun AuthBottomWaveBackground(
    modifier: Modifier = Modifier,
    heightRatio: Float = 0.38f,
    palette: AuthThemePalette = MagentaAuthPalette
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val waveStartY = height * (1f - heightRatio)

        // Back Layer Wave
        val backWavePath = Path().apply {
            moveTo(0f, waveStartY - height * 0.05f)
            cubicTo(
                width * 0.25f, waveStartY - height * 0.10f,
                width * 0.65f, waveStartY + height * 0.02f,
                width, waveStartY - height * 0.04f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = backWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(palette.light.copy(alpha = 0.6f), palette.primary.copy(alpha = 0.8f)),
                startY = waveStartY - height * 0.10f,
                endY = height
            )
        )

        // Front Main Wave
        val frontWavePath = Path().apply {
            moveTo(0f, waveStartY)
            cubicTo(
                width * 0.30f, waveStartY + height * 0.06f,
                width * 0.70f, waveStartY - height * 0.06f,
                width, waveStartY
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = frontWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(palette.primary, palette.dark),
                startY = waveStartY - height * 0.05f,
                endY = height
            )
        )
    }
}

/**
 * Renders the Get Started large multi-layered bottom wave
 */
@Composable
fun GetStartedBottomWaveBackground(
    modifier: Modifier = Modifier,
    palette: AuthThemePalette = MagentaAuthPalette
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val waveStartY = height * 0.52f

        // Layer 1 - Deep soft wave
        val wave1 = Path().apply {
            moveTo(0f, waveStartY - 30f)
            cubicTo(
                width * 0.35f, waveStartY - 80f,
                width * 0.75f, waveStartY + 40f,
                width, waveStartY - 20f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = wave1,
            color = palette.light.copy(alpha = 0.5f)
        )

        // Layer 2 - Front prominent wave
        val wave2 = Path().apply {
            moveTo(0f, waveStartY + 20f)
            cubicTo(
                width * 0.40f, waveStartY - 40f,
                width * 0.80f, waveStartY + 30f,
                width, waveStartY - 10f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = wave2,
            brush = Brush.verticalGradient(
                colors = listOf(palette.primary, palette.dark),
                startY = waveStartY,
                endY = height
            )
        )
    }
}
