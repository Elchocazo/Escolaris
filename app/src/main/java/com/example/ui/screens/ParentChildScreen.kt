package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TardyRecordEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.BadgeItem
import com.example.domain.model.TaskStatus
import com.example.domain.validation.ValidationUtils
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentChildScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    val tardyRecords by viewModel.tardyRecords.collectAsState()
    val allPenalties by viewModel.allPenalties.collectAsState()

    val linkedStudentId = currentUser?.linkedStudentId.orEmpty()
    val student = remember(allUsers, linkedStudentId) {
        if (linkedStudentId.isNotBlank()) {
            allUsers.find { it.id == linkedStudentId || (it.studentCode.isNotBlank() && it.studentCode.equals(linkedStudentId, ignoreCase = true)) }
        } else null
    }

    var showLinkDialog by remember { mutableStateOf(false) }
    var showUnlinkConfirmDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 📊 Académico, 1: 🪙 Billetera Escolaris, 2: 🎖️ Méritos & Asistencia
    var selectedBadgeDetail by remember { mutableStateOf<BadgeEntity?>(null) }
    var selectedPokemonBadgeDetail by remember { mutableStateOf<BadgeItem?>(null) }
    var transferAmountInput by remember { mutableStateOf("25") }
    var transferReasonInput by remember { mutableStateOf("¡Felicitaciones por tu esfuerzo y compromiso académico!") }

    val parentBalance = (currentUser?.parentIncentiveCredits ?: 100).coerceAtLeast(currentUser?.credits ?: 0)

    // Insignias del ESTUDIANTE VINCULADO (Filtradas con rol STUDENT -> Únicamente ACADEMIC y STUDENT, ¡nunca del acudiente!)
    val studentPokemonBadges = remember {
        filterBadgesForRole("STUDENT", DEFAULT_ESCOLARIS_BADGES)
    }
    val studentEarnedBadges = remember(allBadges, student?.id) {
        val sId = student?.id.orEmpty()
        if (sId.isNotBlank()) {
            allBadges.filter {
                it.studentId == sId &&
                (it.category.equals("ACADEMIC", ignoreCase = true) ||
                 it.category.equals("STUDENT", ignoreCase = true) ||
                 (!it.category.equals("PARENT", ignoreCase = true) && !it.category.equals("FAMILY", ignoreCase = true)))
            }
        } else emptyList()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // =========================================================================
            // CASO 1: NO VINCULADO (linkedStudentId vacío o estudiante no encontrado)
            // =========================================================================
            if (student == null) {
                item {
                    UnlinkedStudentCard(
                        onOpenLinkDialog = { showLinkDialog = true }
                    )
                }
            } else {
                // =========================================================================
                // CASO 2: ESTUDIANTE VINCULADO
                // =========================================================================
                // 1. Encabezado del Estudiante
                item {
                    StudentHeaderCard(
                        student = student,
                        onUnlinkRequest = { showUnlinkConfirmDialog = true }
                    )
                }

                // 2. Barra de Pestañas
                item {
                    val tabs = listOf(
                        "📊 Académico",
                        "🪙 Billetera",
                        "🎖️ Méritos"
                    )

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }
                }

                // =====================================================================
                // TAB 0: 📊 SEGUIMIENTO ACADÉMICO & TAREAS
                // =====================================================================
                if (selectedTab == 0) {
                    val studentTasks = tasks.filter { it.studentId == student.id }
                    val studentExams = exams.filter { it.studentId == student.id }
                    val gradedExams = studentExams.filter { it.isGraded && it.grade != null }
                    val averageGrade = if (gradedExams.isNotEmpty()) {
                        gradedExams.map { it.grade ?: 0.0 }.average()
                    } else 0.0

                    val completedTasks = studentTasks.count { it.status == TaskStatus.COMPLETED.code || it.completed }
                    val totalTasks = studentTasks.size.coerceAtLeast(1)
                    val taskPercent = if (studentTasks.isNotEmpty()) (completedTasks * 100) / totalTasks else 100
                    val pendingTasks = studentTasks.filter { !it.completed && it.status != TaskStatus.COMPLETED.code }
                    val upcomingExams = studentExams.filter { !it.isGraded }

                    // Métricas Rápidas
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // GPA
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Promedio GPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (gradedExams.isNotEmpty()) String.format(Locale.US, "%.1f / 5.0", averageGrade) else "S/C",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (averageGrade >= 3.5) SuccessGreen else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (gradedExams.isNotEmpty()) "${gradedExams.size} evaluada(s)" else "Sin notas aún",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            // Tareas
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Cumplimiento", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$taskPercent%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (taskPercent >= 80) SuccessGreen else StreakOrange
                                    )
                                    Text(
                                        text = "$completedTasks de ${studentTasks.size} tareas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    // Tareas Pendientes
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("📚", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tareas Pendientes de Entrega (${pendingTasks.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (pendingTasks.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SuccessGreen.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("¡Todo al día!", fontWeight = FontWeight.Bold, color = SuccessGreen)
                                        Text("Tu hijo/a no tiene tareas escolares pendientes en este momento.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    } else {
                        items(pendingTasks, key = { it.id }) { task ->
                            TaskItemCard(task = task)
                        }
                    }

                    // Próximas Evaluaciones Agendadas
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("📝", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Próximas Evaluaciones (${upcomingExams.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (upcomingExams.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No hay exámenes programados próximamente.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    } else {
                        items(upcomingExams, key = { it.id }) { exam ->
                            UpcomingExamCard(exam = exam)
                        }
                    }

                    // Calificaciones Recientes
                    if (gradedExams.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("🎓", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Notas y Evaluaciones Calificadas (${gradedExams.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(gradedExams, key = { it.id }) { exam ->
                            GradedExamCard(exam = exam)
                        }
                    }
                }

                // =====================================================================
                // TAB 1: 🪙 BILLETERA ESCOLARIS ("CEDER CRÉDITOS")
                // =====================================================================
                if (selectedTab == 1) {
                    item {
                        ParentWalletCard(
                            parentBalance = parentBalance,
                            studentName = student.name,
                            studentCredits = student.credits,
                            transferAmountInput = transferAmountInput,
                            onAmountChange = { transferAmountInput = it.filter { ch -> ch.isDigit() } },
                            transferReasonInput = transferReasonInput,
                            onReasonChange = { transferReasonInput = it },
                            onTransfer = { amt, reason ->
                                viewModel.transferParentPointsToChild(amt, reason)
                            }
                        )
                    }
                }

                // =====================================================================
                // TAB 2: 🎖️ MÉRITOS, RECONOCIMIENTOS Y ASISTENCIA
                // =====================================================================
                if (selectedTab == 2) {
                    val studentTardies = tardyRecords.filter { it.studentId == student.id }
                    val studentPenalties = allPenalties.filter { it.studentId == student.id }

                    // Header de Resumen Pokémon GO del Alumno
                    item {
                        val unlockedCount = studentPokemonBadges.count { it.isUnlocked }
                        val totalCount = studentPokemonBadges.size
                        val progressFraction = if (totalCount > 0) (unlockedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
                        val progressPercent = (progressFraction * 100).toInt()

                        PokemonGoShowcaseHeader(
                            unlockedCount = unlockedCount,
                            totalCount = totalCount,
                            progressPercent = progressPercent,
                            progressFraction = progressFraction
                        )
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("🎖️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Medallero Escolar del Estudiante",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Cuadrícula Pokémon GO (3 por fila)
                    studentPokemonBadges.chunked(3).forEach { rowBadges ->
                        item {
                            PokemonGoBadgesRow(
                                badgesInRow = rowBadges,
                                onBadgeClick = { selectedPokemonBadgeDetail = it }
                            )
                        }
                    }

                    // Menciones adicionales otorgadas directamente por docentes al estudiante
                    if (studentEarnedBadges.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏅", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Menciones Otorgadas al Alumno (${studentEarnedBadges.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(studentEarnedBadges, key = { it.id }) { badge ->
                            StudentBadgeRowCard(
                                badge = badge,
                                onClick = { selectedBadgeDetail = badge }
                            )
                        }
                    }

                    // Asistencia & Puntualidad
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("⏰", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Control de Asistencia y Puntualidad",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (studentTardies.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SuccessGreen.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Asistencia Ejemplar", fontWeight = FontWeight.Bold, color = SuccessGreen)
                                        Text("Sin reportes de llegadas tarde ni faltas injustificadas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    } else {
                        items(studentTardies, key = { it.id }) { tardy ->
                            StudentTardyRowCard(tardy = tardy)
                        }
                    }

                    // Sanciones o reportes disciplinarios
                    if (studentPenalties.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("🚨", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reportes Disciplinarios (${studentPenalties.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                            }
                        }

                        items(studentPenalties, key = { it.id }) { penalty ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DangerRed.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(penalty.reason, fontWeight = FontWeight.Bold, color = DangerRed)
                                        Text("-${penalty.pointsDeducted} 🪙", fontWeight = FontWeight.Bold, color = DangerRed)
                                    }
                                    if (penalty.observation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(penalty.observation, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Docente: ${penalty.teacherName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }

    // =========================================================================
    // DIÁLOGOS
    // =========================================================================

    // Diálogo de Vinculación de Estudiante
    if (showLinkDialog) {
        var inputCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔑", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vincular Código de Estudiante", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ingresa el código escolar que aparece en el perfil de tu hijo/a (Ej. ESC-100201) para acceder a sus tareas, notas y cederle Escolaris.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase() },
                        label = { Text("Código de Estudiante") },
                        placeholder = { Text("ESC-XXXXXX") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputCode.isNotBlank()) {
                            viewModel.linkParentToStudent(inputCode)
                            showLinkDialog = false
                        }
                    },
                    enabled = inputCode.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Vincular Cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Confirmación para Desvincular / Cambiar
    if (showUnlinkConfirmDialog && student != null) {
        AlertDialog(
            onDismissRequest = { showUnlinkConfirmDialog = false },
            title = { Text("Cambiar de Estudiante", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Deseas desvincular a ${student.name} para ingresar el código de otro estudiante?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlinkConfirmDialog = false
                        showLinkDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ingresar Otro Código")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Detalle de Medalla
    if (selectedBadgeDetail != null) {
        val badge = selectedBadgeDetail!!
        val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(badge.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(badge.title, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(badge.description, style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldStar.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("⭐ +${badge.xpReward} XP", fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            Text("🪙 +${badge.creditReward} Escolaris", fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        }
                    }
                    if (badge.teacherNote.isNotBlank()) {
                        Text("Docente: ${badge.teacherNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Text("Fecha: ${dateFormat.format(Date(badge.unlockedAtMillis))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            },
            confirmButton = {
                Button(onClick = { selectedBadgeDetail = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Diálogo de Detalle de Medalla Pokémon GO del Estudiante
    selectedPokemonBadgeDetail?.let { badge ->
        PokemonGoBadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedPokemonBadgeDetail = null }
        )
    }
}

// =============================================================================
// COMPONENTES DE VISTA DE APOYO
// =============================================================================

@Composable
private fun UnlinkedStudentCard(
    onOpenLinkDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text("👨‍👧", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Vincula la Cuenta de tu Hijo/a",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Conecta con el código de estudiante escolar para consultar sus calificaciones en tiempo real, supervisar tareas pendientes, justificar tardanzas y premiarlo con Escolaris.",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onOpenLinkDialog,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ingresar Código de Estudiante", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "El código (ejemplo: ESC-100201) se encuentra en la pantalla de Perfil del alumno.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StudentHeaderCard(
    student: UserEntity,
    onUnlinkRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con badge de nivel
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.avatarEmoji.ifBlank { "🎓" },
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ValidationUtils.formatProperNoun(student.name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${student.gradeSection} • Código: ${student.studentCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Nvl. ${student.level} • ${student.xp} XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldStar.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "🪙 ${student.credits}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onUnlinkRequest) {
                Icon(
                    Icons.Default.LinkOff,
                    contentDescription = "Cambiar de estudiante",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ParentWalletCard(
    parentBalance: Int,
    studentName: String,
    studentCredits: Int,
    transferAmountInput: String,
    onAmountChange: (String) -> Unit,
    transferReasonInput: String,
    onReasonChange: (String) -> Unit,
    onTransfer: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.45f)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera Billetera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Billetera Escolaris Familiar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Incentiva y premia el esfuerzo de $studentName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Balances Comparados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GoldStar.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tu Saldo para Ceder", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                        Text(
                            text = "🪙 $parentBalance",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Saldo de $studentName", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "🪙 $studentCredits",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Presets rápidos
            Text("Selecciona una cantidad rápida a transferir:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("10", "25", "50", "100", "200").forEach { preset ->
                    val isSelected = transferAmountInput == preset
                    Button(
                        onClick = { onAmountChange(preset) },
                        colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black) else ButtonDefaults.filledTonalButtonColors(),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+$preset 🪙", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            // Input personalizado
            OutlinedTextField(
                value = transferAmountInput,
                onValueChange = onAmountChange,
                label = { Text("Cantidad personalizada (🪙 Escolaris)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Motivo
            OutlinedTextField(
                value = transferReasonInput,
                onValueChange = onReasonChange,
                label = { Text("Motivo de reconocimiento (Opcional)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            val amountInt = transferAmountInput.toIntOrNull() ?: 0
            val canTransfer = amountInt in 1..parentBalance

            Button(
                onClick = {
                    if (canTransfer) {
                        onTransfer(amountInt, transferReasonInput)
                    }
                },
                enabled = canTransfer,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (amountInt <= 0) "Ingresa una cantidad válida" else if (amountInt > parentBalance) "Saldo insuficiente (🪙 $parentBalance)" else "Ceder +$amountInt 🪙 Escolaris a $studentName 🚀",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

@Composable
private fun TaskItemCard(
    task: TaskEntity,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault())
    val priorityColor = when (task.priority.uppercase()) {
        "ALTA" -> DangerRed
        "MEDIA" -> StreakOrange
        else -> SuccessGreen
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.subject,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = priorityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Prioridad ${task.priority}",
                        fontWeight = FontWeight.Bold,
                        color = priorityColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vence: ${if (task.dueDateMillis > 0) dateFormat.format(Date(task.dueDateMillis)) else task.dueDate.ifBlank { "Próximamente" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = "Recompensa: +${task.rewardCredits} 🪙",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
            }
        }
    }
}

@Composable
private fun UpcomingExamCard(
    exam: ExamEntity,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📝", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${exam.subject} • ${exam.classroom}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (exam.topics.isNotBlank()) {
                    Text(
                        text = "Temas: ${exam.topics}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = dateFormat.format(Date(exam.examDateMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun GradedExamCard(
    exam: ExamEntity,
    modifier: Modifier = Modifier
) {
    val grade = exam.grade ?: 0.0
    val isApproved = grade >= 3.0

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isApproved) SuccessGreen.copy(alpha = 0.3f) else DangerRed.copy(alpha = 0.3f)),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isApproved) SuccessGreen.copy(alpha = 0.12f) else DangerRed.copy(alpha = 0.12f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", grade),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = if (isApproved) SuccessGreen else DangerRed
                        )
                        Text(
                            text = "/ 5.0",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isApproved) SuccessGreen else DangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = exam.subject,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!exam.teacherFeedback.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Docente: \"${exam.teacherFeedback}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentBadgeRowCard(
    badge: BadgeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldStar.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(badge.emoji, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(badge.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(badge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldStar.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "+${badge.creditReward} 🪙",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun StudentTardyRowCard(
    tardy: TardyRecordEntity,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val isJustified = tardy.status.equals("JUSTIFICADO", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isJustified) SuccessGreen.copy(alpha = 0.3f) else StreakOrange.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tardy.subject} • ${tardy.delayMinutes} min de retraso",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isJustified) SuccessGreen.copy(alpha = 0.15f) else StreakOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tardy.status,
                        fontWeight = FontWeight.Bold,
                        color = if (isJustified) SuccessGreen else StreakOrange,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hora: ${tardy.arrivalTime} • Fecha: ${dateFormat.format(Date(tardy.dateMillis))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            if (tardy.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Motivo: ${tardy.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
