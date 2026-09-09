package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.BadgeItem
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SuccessGreen

// =============================================================================
// CATÁLOGO DE METAS ESCOLARES (ESTILO POKÉMON GO)
// Filtradas por Rol: PARENT (reuniones, escuela de padres, pensión) y STUDENT (tareas, exámenes, reconocimientos)
// =============================================================================

// -----------------------------------------------------------------------------
// LISTA MAESTRA OFICIAL DE INSIGNIAS BASE (PROGRESO INICIAL EN CERO)
// -----------------------------------------------------------------------------

val INITIAL_PARENT_BADGES: List<BadgeItem> = listOf(
    BadgeItem(
        id = "badge_parent_report_cards",
        title = "Entrega de Boletines",
        description = "Asistir puntualmente a las 4 entregas oficiales de informes académicos.",
        category = "PARENT",
        currentProgress = 0,
        targetProgress = 4,
        emoji = "📋",
        unlockedAtDate = null,
        xpReward = 250,
        creditReward = 120
    ),
    BadgeItem(
        id = "badge_parent_workshops",
        title = "Escuela de Padres",
        description = "Participar activamente en 2 talleres de formación y escuela de padres.",
        category = "PARENT",
        currentProgress = 0,
        targetProgress = 2,
        emoji = "👨‍👩‍👧",
        unlockedAtDate = null,
        xpReward = 300,
        creditReward = 150
    ),
    BadgeItem(
        id = "badge_parent_pension",
        title = "Compromiso de Pensión",
        description = "Pagar puntualmente la pensión durante 3 meses consecutivos dentro de los primeros 5 días del mes.",
        category = "PARENT",
        currentProgress = 0,
        targetProgress = 3,
        emoji = "💳",
        unlockedAtDate = null,
        xpReward = 350,
        creditReward = 200
    ),
    BadgeItem(
        id = "badge_parent_meetings",
        title = "Reunión de Padres",
        description = "Asistir y participar en las asambleas generales y reuniones informativas de curso con docentes.",
        category = "PARENT",
        currentProgress = 0,
        targetProgress = 2,
        emoji = "🤝",
        unlockedAtDate = null,
        xpReward = 200,
        creditReward = 100
    ),
    BadgeItem(
        id = "badge_parent_support",
        title = "Acompañamiento",
        description = "Supervisión diaria y apoyo formativo en las tareas y deberes escolares en casa.",
        category = "PARENT",
        currentProgress = 0,
        targetProgress = 5,
        emoji = "🏡",
        unlockedAtDate = null,
        xpReward = 180,
        creditReward = 90
    )
)

val INITIAL_STUDENT_BADGES: List<BadgeItem> = listOf(
    BadgeItem(
        id = "badge_student_tasks_10",
        title = "Cumplimiento de Tareas",
        description = "Entregar 10 tareas a tiempo y completas antes del cierre de periodo escolar.",
        category = "ACADEMIC",
        currentProgress = 0,
        targetProgress = 10,
        emoji = "📚",
        unlockedAtDate = null,
        xpReward = 200,
        creditReward = 100
    ),
    BadgeItem(
        id = "badge_student_attendance_20",
        title = "Asistencia Ejemplar",
        description = "Acumular 20 días de asistencia continua sin retardos ni ausencias.",
        category = "STUDENT",
        currentProgress = 0,
        targetProgress = 20,
        emoji = "⏰",
        unlockedAtDate = null,
        xpReward = 220,
        creditReward = 110
    ),
    BadgeItem(
        id = "badge_student_academic_excellence_5",
        title = "Excelencia Académica",
        description = "Aprobar 5 evaluaciones o exámenes bimestrales con calificación sobresaliente (4.5 o más).",
        category = "ACADEMIC",
        currentProgress = 0,
        targetProgress = 5,
        emoji = "⭐",
        unlockedAtDate = null,
        xpReward = 300,
        creditReward = 150
    ),
    BadgeItem(
        id = "badge_student_steam",
        title = "Innovador STEAM",
        description = "Presentar un proyecto destacado en la Feria de Ciencia, Tecnología y Robótica.",
        category = "ACADEMIC",
        currentProgress = 0,
        targetProgress = 1,
        emoji = "🔬",
        unlockedAtDate = null,
        xpReward = 400,
        creditReward = 250
    ),
    BadgeItem(
        id = "badge_student_reading",
        title = "Lector Voraz",
        description = "Completar la lectura y análisis de 6 obras literarias en el Plan Lector escolar.",
        category = "ACADEMIC",
        currentProgress = 0,
        targetProgress = 6,
        emoji = "📖",
        unlockedAtDate = null,
        xpReward = 180,
        creditReward = 90
    ),
    BadgeItem(
        id = "badge_student_civic",
        title = "Líder de Paz",
        description = "Participar en 3 jornadas de mediación escolar y sana convivencia.",
        category = "STUDENT",
        currentProgress = 0,
        targetProgress = 3,
        emoji = "🕊️",
        unlockedAtDate = null,
        xpReward = 200,
        creditReward = 100
    ),
    BadgeItem(
        id = "badge_student_solidarity",
        title = "Compañero Solidario",
        description = "Reconocimiento por trabajo en equipo, empatía y apoyo a compañeros de clase.",
        category = "STUDENT",
        currentProgress = 0,
        targetProgress = 2,
        emoji = "🤝",
        unlockedAtDate = null,
        xpReward = 250,
        creditReward = 120
    )
)

val DEFAULT_ESCOLARIS_BADGES: List<BadgeItem> = INITIAL_PARENT_BADGES + INITIAL_STUDENT_BADGES

/**
 * Retorna las insignias oficiales iniciales con progreso en 0 correspondientes al rol proporcionado.
 */
fun getInitialBadgesForRole(role: String?): List<BadgeItem> = filterBadgesForRole(role, DEFAULT_ESCOLARIS_BADGES)

/**
 * Filtra la lista de insignias según el rol del usuario activo.
 * - Si el rol es PARENT / TUTOR: Carga únicamente insignias de categoría "PARENT" (reuniones, escuela de padres, pensión).
 * - Si el rol es STUDENT: Carga únicamente insignias de categoría "ACADEMIC" y "STUDENT" (tareas, exámenes, reconocimientos).
 * - Otros roles (TEACHER, ADMIN) o nulo: Retorna la lista completa.
 */
fun filterBadgesForRole(
    role: String?,
    badges: List<BadgeItem> = DEFAULT_ESCOLARIS_BADGES
): List<BadgeItem> {
    val normalizedRole = role?.trim()?.uppercase() ?: ""
    val isParentOrTutor = normalizedRole == "PARENT" || normalizedRole == "TUTOR" || normalizedRole == "ACUDIENTE"
    val isStudent = normalizedRole == "STUDENT" || normalizedRole == "ALUMNO" || normalizedRole == "ESTUDIANTE"

    return when {
        isParentOrTutor -> badges.filter { it.category.equals("PARENT", ignoreCase = true) }
        isStudent -> badges.filter {
            it.category.equals("ACADEMIC", ignoreCase = true) || it.category.equals("STUDENT", ignoreCase = true)
        }
        else -> badges
    }
}

// =============================================================================
// COMPOSABLE PRINCIPAL: BadgesGridScreen
// =============================================================================

@Composable
fun BadgesGridScreen(
    userRole: String? = null,
    badges: List<BadgeItem> = DEFAULT_ESCOLARIS_BADGES,
    modifier: Modifier = Modifier,
    onBadgeClick: ((BadgeItem) -> Unit)? = null
) {
    // 1. Filtrar insignias base según el rol activo del usuario
    val roleBadges = remember(badges, userRole) {
        if (!userRole.isNullOrBlank()) {
            filterBadgesForRole(userRole, badges)
        } else {
            badges
        }
    }

    // 2. Extraer categorías adaptativas según las insignias del rol
    val categories = remember(roleBadges) {
        val distinctCats = roleBadges.map { it.category.uppercase() }.distinct()
        val list = mutableListOf("TODAS" to "Todas")
        distinctCats.forEach { cat ->
            when (cat) {
                "PARENT" -> list.add("PARENT" to "👨‍👩‍👧 Padres")
                "ACADEMIC" -> list.add("ACADEMIC" to "📚 Académicas")
                "STUDENT" -> list.add("STUDENT" to "🎓 Estudiante")
                "ATTENDANCE" -> list.add("ATTENDANCE" to "⏰ Asistencia")
                "STEAM" -> list.add("STEAM" to "🔬 STEAM")
                else -> list.add(cat to cat.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
        list
    }

    var selectedCategory by remember(categories) { mutableStateOf("TODAS") }
    var selectedBadgeForDetail by remember { mutableStateOf<BadgeItem?>(null) }

    val filteredBadges = remember(roleBadges, selectedCategory) {
        if (selectedCategory == "TODAS") roleBadges
        else roleBadges.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val totalCount = roleBadges.size
    val unlockedCount = roleBadges.count { it.isUnlocked }
    val globalProgressFraction = if (totalCount > 0) (unlockedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val globalPercent = (globalProgressFraction * 100).toInt()

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header 1: Tarjeta de Progreso General (Estilo Entrenador Pokémon GO)
            item(span = { GridItemSpan(3) }) {
                PokemonGoShowcaseHeader(
                    unlockedCount = unlockedCount,
                    totalCount = totalCount,
                    progressPercent = globalPercent,
                    progressFraction = globalProgressFraction
                )
            }

            // Header 2: Chips Horizontales de Categorías (Adaptadas al rol)
            if (categories.size > 2) {
                item(span = { GridItemSpan(3) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(categories) { (code, label) ->
                            val isSelected = selectedCategory == code
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = code },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // Grilla de Medallas (3 por fila)
            items(filteredBadges, key = { it.id }) { badge ->
                PokemonGoBadgeCell(
                    badge = badge,
                    onClick = {
                        selectedBadgeForDetail = badge
                        onBadgeClick?.invoke(badge)
                    }
                )
            }

            // Espaciador inferior
            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        // Diálogo / Modal de Detalle Estilo Pokémon GO
        selectedBadgeForDetail?.let { badge ->
            PokemonGoBadgeDetailDialog(
                badge = badge,
                onDismiss = { selectedBadgeForDetail = null }
            )
        }
    }
}

/**
 * Renderiza una fila de hasta 3 celdas estilo Pokémon GO, ideal para incrustar en LazyColumn.
 */
@Composable
fun PokemonGoBadgesRow(
    badgesInRow: List<BadgeItem>,
    onBadgeClick: (BadgeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (badge in badgesInRow) {
            Box(modifier = Modifier.weight(1f)) {
                PokemonGoBadgeCell(
                    badge = badge,
                    onClick = { onBadgeClick(badge) }
                )
            }
        }
        val placeholders = (3 - badgesInRow.size).coerceAtLeast(0)
        for (i in 0 until placeholders) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


// =============================================================================
// HEADER DE EXHIBICIÓN DE MEDALLAS (POKÉMON GO SHOWCASE)
// =============================================================================

@Composable
fun PokemonGoShowcaseHeader(
    unlockedCount: Int,
    totalCount: Int,
    progressPercent: Int,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = GoldStar.copy(alpha = 0.18f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏅", fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Medallero Escolaris",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$unlockedCount de $totalCount Medallas Obtenidas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (unlockedCount > 0) GoldStar.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$progressPercent%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = if (unlockedCount > 0) Color(0xFFB45309) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// =============================================================================
// CELDA DE MEDALLA (POKÉMON GO BADGE CELL)
// =============================================================================

@Composable
fun PokemonGoBadgeCell(
    badge: BadgeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = badge.isUnlocked

    // Degradados metálicos estilo Pokémon GO
    val unlockedBorderBrush = Brush.sweepGradient(
        listOf(
            Color(0xFFFDE047), // Oro claro brillante
            Color(0xFFD97706), // Ámbar bronce
            Color(0xFFF59E0B), // Oro puro
            Color(0xFFFEF08A), // Brillo central
            Color(0xFFB45309), // Sombra
            Color(0xFFFDE047)
        )
    )

    val lockedBorderBrush = Brush.sweepGradient(
        listOf(
            Color(0xFFCBD5E1), // Plata claro
            Color(0xFF64748B), // Acero oscuro
            Color(0xFF94A3B8), // Plata medio
            Color(0xFFE2E8F0), // Reflejo
            Color(0xFF475569), // Sombra
            Color(0xFFCBD5E1)
        )
    )

    val discBrush = if (isUnlocked) {
        Brush.radialGradient(
            listOf(
                Color(0xFFFFFBEB), // Luz central cálida
                Color(0xFFFEF3C7), // Ámbar suave
                Color(0xFFFDE68A)  // Oro borde
            )
        )
    } else {
        Brush.radialGradient(
            listOf(
                Color(0xFFF1F5F9), // Gris claro central
                Color(0xFFE2E8F0), // Acero suave
                Color(0xFFCBD5E1)  // Gris oscuro borde
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        // Medalla Circular Grande
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = if (isUnlocked) 8.dp else 2.dp,
                    shape = CircleShape,
                    spotColor = if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f)
                )
                .background(discBrush, CircleShape)
                .border(
                    width = if (isUnlocked) 3.5.dp else 2.5.dp,
                    brush = if (isUnlocked) unlockedBorderBrush else lockedBorderBrush,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Anillo interno decorativo estilo moneda / Poké-Medal
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .border(
                        width = 1.dp,
                        color = if (isUnlocked) Color(0xFFD97706).copy(alpha = 0.45f) else Color(0xFF94A3B8).copy(alpha = 0.45f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Emoji con efecto de desaturación en gris si está bloqueada
                Text(
                    text = badge.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.scale(if (isUnlocked) 1f else 0.92f),
                    color = if (isUnlocked) Color.Unspecified else Color.Gray.copy(alpha = 0.55f)
                )
            }

            // Indicador de estado flotante en la medalla (Candado o Estrella)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFFF59E0B) else Color(0xFF64748B))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Desbloqueada",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Bloqueada",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Título de la Medalla
        Text(
            text = badge.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isUnlocked) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Barra de Progreso Delgada
        LinearProgressIndicator(
            progress = { badge.progressFraction },
            modifier = Modifier
                .width(64.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (isUnlocked) GoldStar else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Avance Numérico
        Text(
            text = "${badge.currentProgress}/${badge.targetProgress}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) Color(0xFFB45309) else MaterialTheme.colorScheme.outline
        )
    }
}

// =============================================================================
// MODAL DE DETALLE CENTRADO (ESTILO POKÉMON GO)
// =============================================================================

@Composable
fun PokemonGoBadgeDetailDialog(
    badge: BadgeItem,
    onDismiss: () -> Unit
) {
    val isUnlocked = badge.isUnlocked

    val unlockedBorderBrush = Brush.sweepGradient(
        listOf(
            Color(0xFFFDE047),
            Color(0xFFD97706),
            Color(0xFFF59E0B),
            Color(0xFFFEF08A),
            Color(0xFFB45309),
            Color(0xFFFDE047)
        )
    )

    val lockedBorderBrush = Brush.sweepGradient(
        listOf(
            Color(0xFFCBD5E1),
            Color(0xFF64748B),
            Color(0xFF94A3B8),
            Color(0xFFE2E8F0),
            Color(0xFF475569),
            Color(0xFFCBD5E1)
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // Tarjeta principal de la medalla
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp)
                ) {
                    // Medalla Circular Grande Central
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .shadow(
                                elevation = if (isUnlocked) 14.dp else 4.dp,
                                shape = CircleShape,
                                spotColor = if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.2f)
                            )
                            .background(
                                if (isUnlocked) {
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFFDE68A))
                                    )
                                } else {
                                    Brush.radialGradient(
                                        listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                    )
                                },
                                CircleShape
                            )
                            .border(
                                width = if (isUnlocked) 5.dp else 3.5.dp,
                                brush = if (isUnlocked) unlockedBorderBrush else lockedBorderBrush,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .border(
                                    width = 1.2.dp,
                                    color = if (isUnlocked) Color(0xFFD97706).copy(alpha = 0.5f) else Color(0xFF94A3B8).copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badge.emoji,
                                fontSize = 48.sp,
                                color = if (isUnlocked) Color.Unspecified else Color.Gray.copy(alpha = 0.55f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Título Grande y Descriptivo
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chip de Categoría
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = when (badge.category.uppercase()) {
                                "ACADEMIC" -> "📚 Académico"
                                "PARENT" -> "👨‍👩‍👧 Padres de Familia"
                                "ATTENDANCE" -> "⏰ Asistencia y Puntualidad"
                                "STEAM" -> "🔬 Ciencia & Tecnología"
                                else -> "🎖️ General"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Texto Explicativo de la Condición
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🎯 Condición de Desbloqueo:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de Progreso y Contador
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progreso Actual",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${badge.currentProgress} de ${badge.targetProgress} (${badge.progressPercent}%)",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUnlocked) SuccessGreen else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { badge.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isUnlocked) SuccessGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estado: Desbloqueada o Faltante
                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "¡Condecoración obtenida!",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SuccessGreen,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (!badge.unlockedAtDate.isNullOrBlank()) {
                                        Text(
                                            text = "Desbloqueada el ${badge.unlockedAtDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val remaining = badge.targetProgress - badge.currentProgress
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StreakOrange.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, StreakOrange.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "En curso: Faltan $remaining objetivo(s) para completar esta medalla.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = StreakOrange,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recompensas
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldStar.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ +${badge.xpReward} XP", fontWeight = FontWeight.Bold, color = Color(0xFFB45309), fontSize = 13.sp)
                            Text("🪙 +${badge.creditReward} Escolaris", fontWeight = FontWeight.Bold, color = Color(0xFFB45309), fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón circular inferior con ícono 'X' (Firma de Pokémon GO)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(52.dp)
                    .clickable(onClick = onDismiss)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
