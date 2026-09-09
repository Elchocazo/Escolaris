package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.RedemptionEntity
import com.example.data.local.entity.RewardEntity
import com.example.domain.model.BadgeItem
import com.example.domain.model.UserRole
import com.example.ui.components.ClayBadgeItem
import com.example.ui.components.LevelAchievementsDialog
import com.example.ui.components.ProfilePhotoViewerDialog
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkThemeMode
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel

data class OfficialBadgeDefinition(
    val key: String,
    val title: String,
    val description: String,
    val emoji: String,
    val category: String, // "ACADEMIC", "CIVIC", "EFFORT", "COMMUNITY", "SPECIAL", "FAMILY"
    val xpReward: Int,
    val creditReward: Int,
    val clayColorHex: Long = 0xFF6366F1
)

val OFFICIAL_BADGES_CATALOG = listOf(
    OfficialBadgeDefinition(
        key = "FLAG_RAISING",
        title = "Izada de Bandera",
        description = "Honor patrio, rendimiento académico y convivencia escolar ejemplar.",
        emoji = "🇨🇴",
        category = "STUDENT",
        xpReward = 200,
        creditReward = 100,
        clayColorHex = 0xFF2563EB
    ),
    OfficialBadgeDefinition(
        key = "FIRST_GRADE_5",
        title = "Nota Sobresaliente (5.0)",
        description = "Calificación excelente en evaluación o taller académico.",
        emoji = "⭐",
        category = "ACADEMIC",
        xpReward = 250,
        creditReward = 100,
        clayColorHex = 0xFFEAB308
    ),
    OfficialBadgeDefinition(
        key = "PERFECT_ATTENDANCE",
        title = "Puntualidad de Oro",
        description = "Llegada a tiempo y cero retardos en la jornada escolar.",
        emoji = "⏰",
        category = "STUDENT",
        xpReward = 150,
        creditReward = 80,
        clayColorHex = 0xFFF59E0B
    ),
    OfficialBadgeDefinition(
        key = "PEER_HELPER",
        title = "Compañero Solidario",
        description = "Solidaridad, ayuda en clase y trabajo en equipo respetuoso.",
        emoji = "🤝",
        category = "STUDENT",
        xpReward = 180,
        creditReward = 80,
        clayColorHex = 0xFF10B981
    ),
    OfficialBadgeDefinition(
        key = "BOOK_DEVOURER",
        title = "Lector Entusiasta",
        description = "Hábito lector constante y participación reflexiva en español.",
        emoji = "📖",
        category = "ACADEMIC",
        xpReward = 160,
        creditReward = 75,
        clayColorHex = 0xFF6366F1
    ),
    OfficialBadgeDefinition(
        key = "STEAM_CREATIVITY",
        title = "Científico Escolar",
        description = "Curiosidad e indagación en ciencias naturales y química.",
        emoji = "🔬",
        category = "ACADEMIC",
        xpReward = 180,
        creditReward = 90,
        clayColorHex = 0xFF8B5CF6
    ),
    OfficialBadgeDefinition(
        key = "ARTISTIC_EXPRESSION",
        title = "Expresión Artística",
        description = "Creatividad y talento destacado en artes plásticas y música.",
        emoji = "🎨",
        category = "STUDENT",
        xpReward = 180,
        creditReward = 85,
        clayColorHex = 0xFFEC4899
    ),
    OfficialBadgeDefinition(
        key = "ECO_HERO",
        title = "Cuidado del Salón",
        description = "Puesto ordenado, aula limpia y respeto al mobiliario.",
        emoji = "🌱",
        category = "STUDENT",
        xpReward = 150,
        creditReward = 70,
        clayColorHex = 0xFF16A34A
    ),
    OfficialBadgeDefinition(
        key = "ACTIVE_PARTICIPATION",
        title = "Participación Activa",
        description = "Aportes constructivos y entusiasmo durante las clases.",
        emoji = "💡",
        category = "STUDENT",
        xpReward = 170,
        creditReward = 80,
        clayColorHex = 0xFFF59E0B
    ),
    OfficialBadgeDefinition(
        key = "STREAK_7_DAYS",
        title = "Semana Impecable",
        description = "Semana completa con tareas entregadas y disciplina constante.",
        emoji = "🔥",
        category = "STUDENT",
        xpReward = 200,
        creditReward = 90,
        clayColorHex = 0xFFF97316
    )
)

val OFFICIAL_PARENT_BADGES_CATALOG = listOf(
    OfficialBadgeDefinition(
        key = "PARENT_PENSION_OCTUBRE",
        title = "Pensión al Día - Octubre",
        description = "Cancelación puntual de la pensión escolar dentro de los primeros 5 días del mes.",
        emoji = "💳",
        category = "PARENT",
        xpReward = 150,
        creditReward = 100,
        clayColorHex = 0xFF10B981
    ),
    OfficialBadgeDefinition(
        key = "PARENT_MEETING_1",
        title = "Asistencia a Reunión de Padres",
        description = "Participación y asistencia puntual a la asamblea general de padres de familia.",
        emoji = "👨‍👩‍👧",
        category = "PARENT",
        xpReward = 120,
        creditReward = 80,
        clayColorHex = 0xFF2563EB
    ),
    OfficialBadgeDefinition(
        key = "PARENT_COLLABORATOR",
        title = "Escuela de Padres",
        description = "Asistencia y participación formativa en los talleres de orientación y valores familiares.",
        emoji = "🤝",
        category = "PARENT",
        xpReward = 140,
        creditReward = 100,
        clayColorHex = 0xFF8B5CF6
    ),
    OfficialBadgeDefinition(
        key = "PARENT_REPORT_CARD",
        title = "Entrega de Informes Académicos",
        description = "Acompañamiento presencial al estudiante en el balance académico de periodo con docentes.",
        emoji = "📋",
        category = "PARENT",
        xpReward = 120,
        creditReward = 80,
        clayColorHex = 0xFFF59E0B
    ),
    OfficialBadgeDefinition(
        key = "PARENT_HOMEWORK_SUPPORT",
        title = "Acompañamiento en Casa",
        description = "Supervisión constante y apoyo formativo en los deberes y proyectos escolares.",
        emoji = "🏡",
        category = "PARENT",
        xpReward = 100,
        creditReward = 50,
        clayColorHex = 0xFF06B6D4
    ),
    OfficialBadgeDefinition(
        key = "PARENT_EXEMPLARY_FAMILY",
        title = "Familia Ejemplar Escolaris",
        description = "Reconocimiento de honor a la familia por su apoyo integral, respeto y valores comunitarios.",
        emoji = "🏆",
        category = "PARENT",
        xpReward = 250,
        creditReward = 150,
        clayColorHex = 0xFFEAB308
    ),
    OfficialBadgeDefinition(
        key = "PARENT_COMMUNICATION",
        title = "Comunicación Asertiva",
        description = "Diálogo respetuoso, oportuno y constructivo con los docentes y directivos del colegio.",
        emoji = "💬",
        category = "PARENT",
        xpReward = 100,
        creditReward = 50,
        clayColorHex = 0xFFEC4899
    ),
    OfficialBadgeDefinition(
        key = "PARENT_HEALTH_CARE",
        title = "Bienestar y Cuidado Integral",
        description = "Atención permanente a la salud, nutrición y presentación personal del estudiante.",
        emoji = "🍎",
        category = "PARENT",
        xpReward = 110,
        creditReward = 60,
        clayColorHex = 0xFF10B981
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val redemptions by viewModel.redemptions.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val switchedFromParentId by viewModel.switchedFromParentId.collectAsState()

    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedDayForSchedule by remember { mutableIntStateOf(1) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPhotoPreviewDialog by remember { mutableStateOf(false) }
    var showCreateRewardDialog by remember { mutableStateOf(false) }
    var rewardToEdit by remember { mutableStateOf<RewardEntity?>(null) }
    var rewardToDelete by remember { mutableStateOf<RewardEntity?>(null) }
    var selectedRedemptionForQr by remember { mutableStateOf<RedemptionEntity?>(null) }
    var previewBadgePhotoUri by remember { mutableStateOf<String?>(null) }
    var previewBadgeTitle by remember { mutableStateOf("") }
    var selectedBadgeDetail by remember { mutableStateOf<BadgeEntity?>(null) }
    var selectedLockedBadge by remember { mutableStateOf<OfficialBadgeDefinition?>(null) }
    var showLevelAchievementsDialog by remember { mutableStateOf(false) }
    var selectedPokemonBadgeDetail by remember { mutableStateOf<BadgeItem?>(null) }
    var studentStoreSubtab by remember { mutableIntStateOf(0) } // 0: Catálogo, 1: Mis Canjes
    var settingsSubtab by remember { mutableIntStateOf(0) } // 0: Códigos & Aula, 1: Aspecto Visual, 2: Mi Cuenta
    var teacherDirectoryList by remember { mutableStateOf(OFFICIAL_TEACHER_DIRECTORY) }
    var editingTeacher by remember { mutableStateOf<Pair<Int, TeacherDirectoryEntry?>?>(null) }
    var directorySearchQuery by remember { mutableStateOf("") }
    var selectedTeacherDetail by remember { mutableStateOf<Pair<Int, TeacherDirectoryEntry>?>(null) }

    val isTeacher = currentUser?.role == UserRole.TEACHER.code
    val isParent = currentUser?.role == UserRole.PARENT.code || currentUser?.role?.uppercase() == "TUTOR" || currentUser?.role?.uppercase() == "ACUDIENTE"
    val isStudent = currentUser?.role == UserRole.STUDENT.code || currentUser?.role?.uppercase() == "STUDENT" || currentUser?.role?.uppercase() == "ALUMNO"

    val activeOfficialCatalog = if (isParent) OFFICIAL_PARENT_BADGES_CATALOG else OFFICIAL_BADGES_CATALOG

    val userBadges = remember(allBadges, currentUser, isParent, isTeacher, isStudent) {
        if (isParent) {
            // PARENT / TUTOR: Únicamente insignias de categoría "PARENT"
            allBadges.filter {
                (it.studentId == currentUser?.id || it.studentId.isBlank()) &&
                (it.category.equals("PARENT", ignoreCase = true) || it.category.equals("FAMILY", ignoreCase = true))
            }
        } else if (isTeacher) {
            allBadges
        } else {
            // STUDENT: Únicamente insignias de categoría "ACADEMIC" y "STUDENT"
            allBadges.filter {
                it.studentId == currentUser?.id &&
                (it.category.equals("ACADEMIC", ignoreCase = true) || it.category.equals("STUDENT", ignoreCase = true))
            }
        }
    }

    val myRedemptions = remember(redemptions, currentUser, isTeacher) {
        if (isTeacher) redemptions
        else redemptions.filter { it.studentId == currentUser?.id }
    }

    val unlockedBadgesMap = remember(userBadges) {
        userBadges.associateBy { it.badgeKey.uppercase() }
    }
    val unlockedBadgesByTitle = remember(userBadges) {
        userBadges.associateBy { it.title.trim().lowercase() }
    }
    val customTeacherBadges = remember(userBadges, isParent) {
        val activeCatalog = if (isParent) OFFICIAL_PARENT_BADGES_CATALOG else OFFICIAL_BADGES_CATALOG
        val officialKeys = activeCatalog.map { it.key.uppercase() }.toSet()
        val officialTitles = activeCatalog.map { it.title.trim().lowercase() }.toSet()
        userBadges.filter {
            !officialKeys.contains(it.badgeKey.uppercase()) && !officialTitles.contains(it.title.trim().lowercase())
        }
    }
    val allUsers by viewModel.allUsers.collectAsState()
    val linkedStudent = remember(allUsers, currentUser) {
        val linkedId = currentUser?.linkedStudentId ?: ""
        if (linkedId.isNotBlank()) {
            allUsers.find { it.id == linkedId || it.studentCode.equals(linkedId, ignoreCase = true) }
        } else null
    }

    val profileTabs = remember(isTeacher, isParent) {
        if (isParent) {
            listOf(
                "HONOR" to "Logros",
                "DIRECTORY" to "Directorio",
                "SETTINGS" to "Ajustes"
            )
        } else {
            listOf(
                "HONOR" to if (isTeacher) "Honor" else "Logros",
                "STORE" to if (isTeacher) "Tienda" else "Canjes",
                "DIRECTORY" to "Directorio",
                "SETTINGS" to "Ajustes"
            )
        }
    }

    var showLinkStudentDialog by remember { mutableStateOf(false) }

    val roleFilteredPokemonBadges = remember(currentUser?.role) {
        filterBadgesForRole(currentUser?.role, DEFAULT_ESCOLARIS_BADGES)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Hero Profile Card (Limpia y organizada)
                ElevatedCard(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar con visualización ampliada al tocar (tamaño 82.dp)
                            Box(
                                modifier = Modifier
                                    .size(82.dp)
                                    .clip(CircleShape)
                                    .clickable { showPhotoPreviewDialog = true }
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!currentUser?.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = currentUser?.photoUri,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(82.dp).clip(CircleShape)
                                    )
                                } else {
                                    Text(currentUser?.avatarEmoji ?: "🎓", fontSize = 42.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Columna derecha con datos estéticamente organizados
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = com.example.domain.validation.ValidationUtils.formatProperNoun(currentUser?.name ?: "Usuario"),
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable { showEditProfileDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Editar",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = when {
                                        isTeacher -> "👨‍🏫 Docente • ${currentUser?.gradeSection?.ifBlank { "Docente Titular" } ?: "Docente Titular"}"
                                        isParent -> "👨‍👩‍👧 Tutor • ${currentUser?.gradeSection?.ifBlank { "Familia" } ?: "Familia"}"
                                        else -> "🎓 Estudiante • ${currentUser?.gradeSection?.replace(" - Sección A", "") ?: "10° Grado"}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!currentUser?.bio.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = currentUser?.bio ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 14.sp,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Pastillas de métricas compactas
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isTeacher) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("📚", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = currentUser?.gradeSection?.ifBlank { "Docente Titular" } ?: "Docente Titular",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GoldStar.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🎖️", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Honor",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFB45309),
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    } else if (isParent) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GoldStar.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f)),
                                            modifier = Modifier.clickable { selectedTabIndex = 1 }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🪙", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${currentUser?.credits ?: 0}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFB45309),
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { showLevelAchievementsDialog = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("⭐", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Nv. ${currentUser?.level ?: 1}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GoldStar.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f)),
                                            modifier = Modifier.clickable { selectedTabIndex = 1 }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🪙", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${currentUser?.credits ?: 0}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFB45309),
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StreakOrange.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, StreakOrange.copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🔥", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${currentUser?.streakDays ?: 0} días",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = StreakOrange,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            // Tabs dinámicas según rol (Logros / Directorio / Ajustes)
            item {
                val safeTabIndex = selectedTabIndex.coerceIn(0, profileTabs.size - 1)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = safeTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {}
                    ) {
                        profileTabs.forEachIndexed { index, (_, title) ->
                            Tab(
                                selected = safeTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (safeTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            )
                        }
                    }
                }
            }

            val currentTabKey = profileTabs.getOrNull(selectedTabIndex.coerceIn(0, profileTabs.size - 1))?.first ?: "HONOR"

            if (currentTabKey == "HONOR") {
                // BADGES TAB
                if (isTeacher) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎖️", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Muro de Honor Institucional",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Como docente titular, puedes premiar el esfuerzo, liderazgo y disciplina de tus alumnos otorgándoles insignias desde el panel de Control Docente.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    if (allBadges.isNotEmpty()) {
                        items(allBadges, key = { it.id }) { badge ->
                            RecognitionWallCard(
                                badge = badge,
                                onPhotoClick = { photoUri ->
                                    previewBadgePhotoUri = photoUri
                                    previewBadgeTitle = "${badge.emoji} ${badge.title}"
                                },
                                onClick = { selectedBadgeDetail = badge }
                            )
                        }
                    }
                } else {
                    val totalOfficialCount = roleFilteredPokemonBadges.size
                    val unlockedCount = roleFilteredPokemonBadges.count { it.isUnlocked }
                    val progressFloat = if (totalOfficialCount > 0) (unlockedCount.toFloat() / totalOfficialCount.toFloat()).coerceIn(0f, 1f) else 0f
                    val progressPercent = (progressFloat * 100).toInt()

                    // Header con Progreso de Desbloqueo Pokémon GO
                    item {
                        PokemonGoShowcaseHeader(
                            unlockedCount = unlockedCount,
                            totalCount = totalOfficialCount,
                            progressPercent = progressPercent,
                            progressFraction = progressFloat
                        )
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("🏅", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isParent) "Medallero Familiar de Compromiso Escolar" else "Medallero de Retos y Metas Estudiantiles",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Cuadrícula Pokémon GO (3 por fila)
                    roleFilteredPokemonBadges.chunked(3).forEach { rowBadges ->
                        item {
                            PokemonGoBadgesRow(
                                badgesInRow = rowBadges,
                                onBadgeClick = { selectedPokemonBadgeDetail = it }
                            )
                        }
                    }

                    // Reconocimientos especiales e insignias ganadas en el registro oficial
                    if (userBadges.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isParent) "🎖️ Condecoraciones Familiares Obtenidas (${userBadges.size}):" else "🎖️ Reconocimientos Obtenidos (${userBadges.size}):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(userBadges, key = { it.id }) { badge ->
                            RecognitionWallCard(
                                badge = badge,
                                onPhotoClick = { photoUri ->
                                    previewBadgePhotoUri = photoUri
                                    previewBadgeTitle = "${badge.emoji} ${badge.title}"
                                },
                                onClick = { selectedBadgeDetail = badge }
                            )
                        }
                    }

                    // Insignias personalizadas adicionales otorgadas por docentes
                    if (customTeacherBadges.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🎖️ Condecoraciones Especiales Personalizadas (${customTeacherBadges.size}):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(customTeacherBadges, key = { it.id }) { customBadge ->
                            RecognitionWallCard(
                                badge = customBadge,
                                onPhotoClick = { photoUri ->
                                    previewBadgePhotoUri = photoUri
                                    previewBadgeTitle = "${customBadge.emoji} ${customBadge.title}"
                                },
                                onClick = { selectedBadgeDetail = customBadge }
                            )
                        }
                    }
                }
            } else if (currentTabKey == "STORE") {
                // TEACHER & STUDENT VIEW (Pases, Canjes y Tienda Escolar)
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isTeacher) {
                                    Text(
                                        text = "🛍️ Gestión de Tienda y Canjes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Publica recompensas y valida los pases de los alumnos.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "Saldo Disponible: 🪙 ${currentUser?.credits ?: 0} Escolaris",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { studentStoreSubtab = 0 },
                                        colors = if (studentStoreSubtab == 0) ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black) else ButtonDefaults.filledTonalButtonColors(),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (isTeacher) "🛍️ Tienda (${rewards.size})" else "🎁 Catálogo (${rewards.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp, softWrap = false)
                                    }
                                    Button(
                                        onClick = { studentStoreSubtab = 1 },
                                        colors = if (studentStoreSubtab == 1) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.filledTonalButtonColors(),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (isTeacher) "🎟️ Pases (${myRedemptions.size})" else "📜 Mis Canjes (${myRedemptions.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp, softWrap = false)
                                    }
                                }

                                if (isTeacher && studentStoreSubtab == 0) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showCreateRewardDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Crear Nuevo Premio ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (studentStoreSubtab == 0) {
                        // STORE CATALOG
                        if (rewards.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🎁", fontSize = 40.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Catálogo vacío", fontWeight = FontWeight.Bold)
                                        Text(if (isTeacher) "Crea un premio con el botón superior para tus alumnos." else "No hay recompensas escolares registradas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        } else {
                            items(rewards, key = { it.id }) { reward ->
                                val userCredits = currentUser?.credits ?: 0
                                val canAfford = userCredits >= reward.costCredits && reward.stockAvailable > 0
                                RewardItemCard(
                                    reward = reward,
                                    canAfford = canAfford,
                                    isTeacher = isTeacher,
                                    isParent = false,
                                    onRedeem = { viewModel.redeemReward(reward) },
                                    onEdit = { rewardToEdit = reward },
                                    onDelete = { rewardToDelete = reward }
                                )
                            }
                        }
                    } else {
                        // REDEMPTIONS LIST
                        if (myRedemptions.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🎟️", fontSize = 40.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isTeacher) "No hay pases pendientes por validar" else "No tienes pases ni canjes activos",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isTeacher) "Cuando un estudiante canjee un pase, aparecerá aquí." else "Puedes canjear beneficios escolares en el Catálogo Escolar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(myRedemptions, key = { it.id }) { redemption ->
                                RedemptionItemCard(
                                    redemption = redemption,
                                    isTeacher = isTeacher,
                                    onShowQr = { selectedRedemptionForQr = redemption },
                                    onValidate = { viewModel.validateStudentRedemption(redemption.id) }
                                )
                            }
                        }
                    }
            } else if (currentTabKey == "DIRECTORY") {
                // TEACHER DIRECTORY TAB (📞 Directorio)
                teacherDirectoryContentItems(
                    teachers = teacherDirectoryList,
                    searchQuery = directorySearchQuery,
                    onSearchQueryChange = { directorySearchQuery = it },
                    canEdit = isTeacher,
                    context = context,
                    onAddTeacher = if (isTeacher) { { editingTeacher = Pair(-1, null) } } else null,
                    onEditTeacher = if (isTeacher) { { index, teacher -> editingTeacher = Pair(index, teacher) } } else null,
                    onDeleteTeacher = if (isTeacher) { { index ->
                        if (index in teacherDirectoryList.indices) {
                            teacherDirectoryList = teacherDirectoryList.toMutableList().apply { removeAt(index) }
                        }
                    } } else null,
                    onSelectTeacher = { index, teacher ->
                        selectedTeacherDetail = Pair(index, teacher)
                    }
                )
            } else {
                // SETTINGS & THEMES TAB (selectedTabIndex == 3)
                item {
                    val clipboardManager = LocalClipboardManager.current
                    val studentCodeDisplay = currentUser?.studentCode?.ifBlank { "ESC-${(currentUser?.id ?: "000000").takeLast(6).uppercase()}" } ?: "ESC-100201"

                    // SUBTABS HORIZONTALES PARA AJUSTES
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(0, "🔑", if (isTeacher) "Aula" else "Códigos"),
                            Triple(1, "🎨", "Aspecto"),
                            Triple(2, "⚙️", "Cuenta")
                        ).forEach { (idx, icon, label) ->
                            val isSelected = settingsSubtab == idx
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { settingsSubtab = idx }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(icon, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (settingsSubtab) {
                        0 -> {
                            // SECCIÓN CÓDIGOS & AULA
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                shadowElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if (isTeacher) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                            Text(
                                                text = "Asignación de Aula & Dirección",
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = "DIRECTOR DE GRUPO",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Grado Séptimo (7°)",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Colegio Hogar Madre de Dios • Periodo 2026-2027",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                            Text(
                                                text = if (isParent) "Código de Estudiante Vinculado" else "Código de Vinculación Familiar",
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = if (isParent)
                                                "Código del estudiante asociado a tu cuenta parental para el seguimiento de tareas, notas y asistencia."
                                            else
                                                "Comparte este código con tus padres o acudientes para que puedan vincular su cuenta y estar al día con tus tareas y logros.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "CÓDIGO ÚNICO",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = studentCodeDisplay,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    letterSpacing = 1.5.sp
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(studentCodeDisplay))
                                                    viewModel.showMessage("📋 Código copiado al portapapeles: $studentCodeDisplay")
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Copiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                            // SECCIÓN ASPECTO VISUAL
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Aspecto Visual y Modo Oscuro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.setThemeMode(DarkThemeMode.LIGHT) },
                                            colors = if (themeMode == DarkThemeMode.LIGHT) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                                            modifier = Modifier.weight(1f).testTag("theme_mode_light")
                                        ) {
                                            Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Modo Claro", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.setThemeMode(DarkThemeMode.DARK) },
                                            colors = if (themeMode == DarkThemeMode.DARK) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                                            modifier = Modifier.weight(1f).testTag("theme_mode_dark")
                                        ) {
                                            Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Modo Oscuro", fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("Paleta de Color Institucional", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AppColorTheme.entries.forEach { cTheme ->
                                            val isSelected = colorTheme == cTheme
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) cTheme.primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                border = BorderStroke(1.dp, if (isSelected) cTheme.primaryColor else Color.Transparent),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { viewModel.setColorTheme(cTheme) }
                                                    .testTag("color_theme_${cTheme.name}")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 10.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .clip(CircleShape)
                                                            .background(cTheme.primaryColor)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(cTheme.displayName.take(8), fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    2 -> {
                            // SECCIÓN MI CUENTA
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Gestión de Cuenta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (currentUser?.role == UserRole.PARENT.code) {
                                        val linkedStudent = allUsers.find { it.id == currentUser?.linkedStudentId }
                                        if (linkedStudent != null) {
                                            Button(
                                                onClick = { viewModel.switchToChildProfile(linkedStudent.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Cambiar a perfil de ${linkedStudent.name.split(" ").firstOrNull() ?: linkedStudent.name} 🎓", fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    } else if (currentUser?.role == UserRole.STUDENT.code) {
                                        val parentUser = if (!switchedFromParentId.isNullOrBlank()) {
                                            allUsers.find { it.id == switchedFromParentId }
                                        } else {
                                            allUsers.find { it.role == UserRole.PARENT.code && it.linkedStudentId == currentUser?.id }
                                        }
                                        if (parentUser != null) {
                                            Button(
                                                onClick = {
                                                    if (!switchedFromParentId.isNullOrBlank()) {
                                                        viewModel.switchBackToParentProfile()
                                                    } else {
                                                        viewModel.switchUser(parentUser.id)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Volver a perfil de Acudiente (${parentUser.name.split(" ").firstOrNull() ?: ""}) 👨‍👩‍👧", fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }

                                    Button(
                                        onClick = { showEditProfileDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Editar Información del Perfil", fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { viewModel.signOutUser() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("profile_sign_out_button")
                                    ) {
                                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showEditProfileDialog) {
        currentUser?.let { user ->
            EditProfileDialog(
                user = user,
                onDismiss = { showEditProfileDialog = false },
                onSave = { name, bio, emoji, bannerIdx, gradeOrCargo, photoUri, phoneNumber ->
                    viewModel.updateUserProfile(
                        userId = user.id,
                        name = name,
                        bio = bio,
                        avatarEmoji = emoji,
                        avatarColorHex = user.avatarColorHex,
                        photoUri = photoUri,
                        bannerGradientIndex = bannerIdx,
                        gradeSection = gradeOrCargo,
                        phoneNumber = phoneNumber
                    )
                }
            )
        }
    }

    selectedBadgeDetail?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadgeDetail = null }
        )
    }

    if (showPhotoPreviewDialog) {
        currentUser?.let { user ->
            ProfilePhotoViewerDialog(
                photoUri = user.photoUri,
                avatarEmoji = user.avatarEmoji,
                userName = user.name,
                userRole = user.role,
                gradeSection = user.gradeSection,
                onEditProfile = { showEditProfileDialog = true },
                onDismiss = { showPhotoPreviewDialog = false }
            )
        }
    }

    previewBadgePhotoUri?.let { photoUri ->
        ProfilePhotoViewerDialog(
            photoUri = photoUri,
            avatarEmoji = "🎖️",
            userName = previewBadgeTitle.ifBlank { "Reconocimiento de Honor" },
            userRole = "Condecoración Oficial",
            gradeSection = "Muro de Honor Escolar",
            onDismiss = { previewBadgePhotoUri = null }
        )
    }

    selectedRedemptionForQr?.let { redemption ->
        PassQrDialog(
            redemption = redemption,
            onDismiss = { selectedRedemptionForQr = null }
        )
    }

    selectedLockedBadge?.let { def ->
        LockedBadgeDetailDialog(
            badgeDef = def,
            onDismiss = { selectedLockedBadge = null }
        )
    }

    if (showCreateRewardDialog) {
        CreateRewardDialog(
            onDismiss = { showCreateRewardDialog = false },
            onCreate = { title, desc, cost, cat, stock ->
                viewModel.createNewTeacherReward(title, desc, cost, cat, "🎁", stock)
            }
        )
    }

    rewardToEdit?.let { targetReward ->
        EditRewardDialog(
            reward = targetReward,
            onDismiss = { rewardToEdit = null },
            onSave = { title, desc, cost, category, stock ->
                viewModel.updateTeacherReward(
                    rewardId = targetReward.id,
                    title = title,
                    desc = desc,
                    cost = cost,
                    category = category,
                    stock = stock,
                    icon = targetReward.iconKey
                )
                rewardToEdit = null
            }
        )
    }

    rewardToDelete?.let { targetReward ->
        AlertDialog(
            onDismissRequest = { rewardToDelete = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
                    Text("¿Eliminar Recompensa?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar permanentemente '${targetReward.title}' de la tienda escolar? Los estudiantes ya no podrán canjearla.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReward(targetReward)
                        rewardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { rewardToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLinkStudentDialog) {
        var studentCodeInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLinkStudentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👨‍👩‍👧", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vincular Estudiante", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ingresa el código único que aparece en el perfil de tu hijo/a (Ejemplo: ESC-100201).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = studentCodeInput,
                        onValueChange = { studentCodeInput = it.uppercase() },
                        label = { Text("Código de Estudiante (ESC-XXXXXX)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentCodeInput.isNotBlank()) {
                            viewModel.linkParentToStudent(studentCodeInput.trim())
                            showLinkStudentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Vincular")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkStudentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLevelAchievementsDialog && currentUser != null) {
        val unlockedTitles = userBadges.map { it.title }.toSet()
        LevelAchievementsDialog(
            currentLevel = currentUser?.level ?: 1,
            currentXp = currentUser?.xp ?: 0,
            unlockedBadgeTitles = unlockedTitles,
            onDismiss = { showLevelAchievementsDialog = false }
        )
    }

    selectedTeacherDetail?.let { (idx, teacher) ->
        TeacherDetailDialog(
            teacher = teacher,
            onDismiss = { selectedTeacherDetail = null },
            onEdit = if (isTeacher) { { editingTeacher = Pair(idx, teacher) } } else null,
            onDelete = if (isTeacher) { {
                if (idx in teacherDirectoryList.indices) {
                    teacherDirectoryList = teacherDirectoryList.toMutableList().apply { removeAt(idx) }
                }
                selectedTeacherDetail = null
            } } else null
        )
    }

    if (editingTeacher != null) {
        val (index, teacher) = editingTeacher!!
        EditTeacherDialog(
            initialTeacher = teacher,
            onDismiss = { editingTeacher = null },
            onSave = { updatedTeacher ->
                val list = teacherDirectoryList.toMutableList()
                if (index in list.indices) {
                    list[index] = updatedTeacher
                } else {
                    list.add(updatedTeacher)
                }
                teacherDirectoryList = list
                editingTeacher = null
            }
        )
    }

    selectedPokemonBadgeDetail?.let { badge ->
        PokemonGoBadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedPokemonBadgeDetail = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileDialog(
    user: com.example.data.local.entity.UserEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, bio: String, emoji: String, bannerIdx: Int, gradeOrCargo: String, photoUri: String?, phoneNumber: String) -> Unit
) {
    val isTeacher = user.role == UserRole.TEACHER.code
    var name by remember { mutableStateOf(user.name) }
    var gradeOrCargo by remember { mutableStateOf(user.gradeSection) }
    var bio by remember { mutableStateOf(user.bio) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var selectedEmoji by remember { mutableStateOf(user.avatarEmoji) }
    var currentPhotoUri by remember { mutableStateOf<String?>(user.photoUri) }
    var selectedBannerIdx by remember { mutableIntStateOf(user.bannerGradientIndex) }

    val dialogPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { currentPhotoUri = it.toString() }
    }

    val emojis = if (isTeacher) {
        listOf("👨‍🏫", "👩‍🏫", "📚", "📐", "🔬", "🎨", "⚽", "⭐", "👑")
    } else {
        listOf("🎓", "👨‍🎓", "👩‍🎓", "🧑‍💻", "👩‍🔬", "👨‍🚀", "👑", "🎨", "⚽")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isTeacher) "Perfil de Docente" else "Personalizar Perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sección de Foto de Perfil
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentPhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = currentPhotoUri,
                                contentDescription = "Foto Actual",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(60.dp).clip(CircleShape)
                            )
                        } else {
                            Text(selectedEmoji, fontSize = 28.sp)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { dialogPhotoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Elegir Foto de Galería", fontSize = 12.sp)
                        }

                        if (!currentPhotoUri.isNullOrBlank()) {
                            TextButton(
                                onClick = { currentPhotoUri = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Usar Emoji en su lugar", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Tu nombre") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = gradeOrCargo,
                    onValueChange = { gradeOrCargo = it },
                    label = { Text(if (isTeacher) "Asignatura / Grados a Cargo" else "Grado") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Teléfono / Celular (Notificaciones)") },
                    placeholder = { Text("Ej: 300 123 4567") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(if (isTeacher) "Biografía o Mensaje a Estudiantes" else "Biografía / Estado Escolar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text("Selecciona tu Emoji de Avatar:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    emojis.forEach { em ->
                        val isSelected = selectedEmoji == em
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp).clickable { selectedEmoji = em }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(em, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = com.example.domain.validation.ValidationUtils.formatProperNoun(name.trim().ifBlank { "Estudiante" })
                    val cleanGrade = if (isTeacher) gradeOrCargo.trim() else gradeOrCargo.trim().replace(" - Sección A", "").ifBlank { "10° Grado" }
                    onSave(cleanName, bio.trim(), selectedEmoji, selectedBannerIdx, cleanGrade, currentPhotoUri, phoneNumber.trim())
                    onDismiss()
                },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun BadgeDetailDialog(
    badge: BadgeEntity,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(badge.emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(badge.title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(badge.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Otorgada por: ${badge.unlockedByTeacher}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                if (badge.teacherNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nota de honor: \"${badge.teacherNote}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                if (!badge.photoUri.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = badge.photoUri,
                        contentDescription = "Foto de condecoración",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Fecha: ${dateFormat.format(Date(badge.unlockedAtMillis))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun RecognitionWallCard(
    badge: BadgeEntity,
    onPhotoClick: (String) -> Unit,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
    val dateStr = try {
        dateFormat.format(Date(badge.unlockedAtMillis))
    } catch (e: Exception) {
        "Reciente"
    }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del Reconocimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(badge.clayColorHex).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badge.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = badge.title,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (badge.category == "FAMILY") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "👨‍👩‍👧 Familia",
                                    color = Color(0xFF7C3AED),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (badge.category == "FAMILY") "Logro familiar • $dateStr" else "Otorgado el $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldStar.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐ +${badge.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Descripción y motivo del honor
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mensaje / Cita del docente
            if (badge.teacherNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✍️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Palabras del Docente (${badge.unlockedByTeacher}):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${badge.teacherNote}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Foto de evidencia del reconocimiento (Tomada con cámara o subida de galería)
            if (!badge.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPhotoClick(badge.photoUri) }
                ) {
                    AsyncImage(
                        model = badge.photoUri,
                        contentDescription = "Foto del reconocimiento",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tocar para ampliar", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LockedRecognitionCard(
    badgeDef: OfficialBadgeDefinition,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.42f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badgeDef.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badgeDef.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "🔒 Bloqueada",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ +${badgeDef.xpReward} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Requisitos / Reto para desbloquear
            Text(
                text = if (badgeDef.category == "FAMILY") badgeDef.description else "🎯 Reto: ${badgeDef.description}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🪙 Recompensa: +${badgeDef.creditReward} 🪙",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )

                Text(
                    text = if (badgeDef.category == "FAMILY") "Ver detalles 🔍" else "Ver reto 🔍",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun LockedBadgeDetailDialog(
    badgeDef: OfficialBadgeDefinition,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(badgeDef.emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(badgeDef.title, fontWeight = FontWeight.Bold)
                    Text(
                        text = "🔒 Insignia por Desbloquear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (badgeDef.category == "FAMILY") "📋 Criterio de Condecoración" else "🎯 ¿Cómo ganar esta condecoración?",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = badgeDef.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldStar.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Experiencia", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                            Text("⭐ +${badgeDef.xpReward} XP", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFFB45309))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Recompensa", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                            Text("🪙 +${badgeDef.creditReward}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFFB45309))
                        }
                    }
                }

                if (badgeDef.category != "FAMILY") {
                    Text(
                        text = "💡 Consejo: Participa activamente en clase, entrega tus tareas puntualmente y demuestra excelencia y compañerismo para que tu docente te otorgue esta medalla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (badgeDef.category == "FAMILY") "Entendido" else "¡Entendido, voy por ella! 🚀")
            }
        }
    )
}
