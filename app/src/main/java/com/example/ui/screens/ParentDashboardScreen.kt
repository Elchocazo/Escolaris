package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ParentObligationEntity
import com.example.data.local.entity.TardyRecordEntity
import com.example.domain.model.TaskStatus
import com.example.ui.components.WhatsAppPensionReminderCard
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel
import com.example.utils.PdfExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentDashboardScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allUsers by viewModel.allUsers.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val tardyRecords by viewModel.tardyRecords.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val parentObligations by viewModel.parentObligations.collectAsState()

    var showLinkStudentDialog by remember { mutableStateOf(false) }
    var showTransferPointsDialog by remember { mutableStateOf(false) }
    var selectedBadgeDetail by remember { mutableStateOf<BadgeEntity?>(null) }
    var selectedParentTab by remember { mutableIntStateOf(0) } // 0: Pendientes, 1: Cumplidas & Medallas, 2: Académico
    var obligationToComplete by remember { mutableStateOf<ParentObligationEntity?>(null) }
    var celebrationObligation by remember { mutableStateOf<ParentObligationEntity?>(null) }

    val linkedStudentId = currentUser?.linkedStudentId ?: ""
    val student = allUsers.find { it.id == linkedStudentId }
    val isLinked = student != null

    val userObligations = remember(parentObligations, linkedStudentId, currentUser) {
        parentObligations.filter {
            it.parentId == "ALL" || it.parentId == currentUser?.id || it.studentId == "ALL" || it.studentId == linkedStudentId
        }
    }

    val pendingObligations = remember(userObligations) {
        userObligations.filter { !it.isCompleted }
    }
    val completedObligationsList = remember(userObligations) {
        userObligations.filter { it.isCompleted }
    }

    val pendingCount = pendingObligations.size
    val completedCount = completedObligationsList.size

    val pendingPensionObligation = remember(userObligations) {
        userObligations.find { it.category == "PENSION" && !it.isCompleted }
    }

    val totalObligations = userObligations.size.coerceAtLeast(1)
    val completedObligations = userObligations.count { it.isCompleted }
    val obligationsCompletionPercentage = if (userObligations.isNotEmpty()) (completedObligations * 100) / totalObligations else 0

    val familyBadges = remember(allBadges, linkedStudentId, currentUser) {
        allBadges.filter {
            (it.studentId == currentUser?.id || (linkedStudentId.isNotBlank() && it.studentId == linkedStudentId)) &&
            (it.category.equals("FAMILY", ignoreCase = true) || it.category.equals("PARENT", ignoreCase = true) || it.badgeKey.startsWith("PARENT_") || it.badgeKey.startsWith("FAMILY_"))
        }
    }

    val studentTasks = remember(tasks, linkedStudentId) {
        if (linkedStudentId.isNotBlank()) tasks.filter { it.studentId == linkedStudentId } else emptyList()
    }

    val studentExams = remember(exams, linkedStudentId) {
        if (linkedStudentId.isNotBlank()) exams.filter { it.studentId == linkedStudentId } else emptyList()
    }

    val studentTardies = remember(tardyRecords, linkedStudentId) {
        if (linkedStudentId.isNotBlank()) tardyRecords.filter { it.studentId == linkedStudentId } else emptyList()
    }

    val allPenalties by viewModel.allPenalties.collectAsState()
    val studentPenalties = remember(allPenalties, linkedStudentId) {
        if (linkedStudentId.isNotBlank()) allPenalties.filter { it.studentId == linkedStudentId } else emptyList()
    }

    val gradedExams = studentExams.filter { it.isGraded && it.grade != null }
    val averageGrade = if (gradedExams.isNotEmpty()) {
        gradedExams.map { it.grade ?: 0.0 }.average()
    } else 0.0

    val completedTasksCount = studentTasks.count { it.status == TaskStatus.COMPLETED.code }
    val totalTasksCount = studentTasks.size.coerceAtLeast(1)
    val taskCompletionPercentage = if (studentTasks.isNotEmpty()) (completedTasksCount * 100) / totalTasksCount else 0

    val parentBalance = (currentUser?.parentIncentiveCredits ?: 100).coerceAtLeast(currentUser?.credits ?: 0)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Compact Aesthetic Parent Hero Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FamilyRestroom,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Portal de Acudientes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    if (isLinked) {
                                        Text(
                                            text = "Seguimiento de: ${com.example.domain.validation.ValidationUtils.formatProperNoun(student?.name ?: "")} (${student?.gradeSection ?: ""})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = "⚠️ Sin estudiante vinculado",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StreakOrange
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (isLinked) {
                                    Button(
                                        onClick = {
                                            student?.let {
                                                PdfExporter.generateAndShareAcademicReport(
                                                    context = context,
                                                    student = it,
                                                    exams = studentExams,
                                                    tasks = studentTasks,
                                                    schedules = schedules
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("parent_download_pdf_button")
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showLinkStudentDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isLinked) "Cambiar" else "Vincular", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Modo Estudiante en Teléfono del Acudiente
            if (isLinked && student != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📱", fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "¿Tu hijo/a usa este teléfono?",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Activa el modo estudiante para ${com.example.domain.validation.ValidationUtils.formatProperNoun(student.name.split(" ").firstOrNull() ?: student.name)} (horario, tareas, puntos y medallas)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.switchToChildProfile(student.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Entrar 🎓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Si no está vinculado, mostrar tarjeta instructiva para ingresar código
            if (!isLinked) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔑", fontSize = 34.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Conecta la cuenta de tu hijo/a",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ingresa el código único del estudiante para consultar sus calificaciones, tareas pendientes y asistencias.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showLinkStudentDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ingresar Código de Estudiante", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Sub-Barra de Pestañas del Acudiente con Badge de Pendientes
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TabRow(
                            selectedTabIndex = selectedParentTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedParentTab == 0,
                                onClick = { selectedParentTab = 0 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("⏳ Pendientes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (pendingCount > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = DangerRed
                                            ) {
                                                Text(
                                                    text = "$pendingCount",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                            Tab(
                                selected = selectedParentTab == 1,
                                onClick = { selectedParentTab = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("✅ Al Día", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Surface(
                                            shape = CircleShape,
                                            color = SuccessGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "$completedCount",
                                                color = SuccessGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            )
                            Tab(
                                selected = selectedParentTab == 2,
                                onClick = { selectedParentTab = 2 },
                                text = {
                                    Text("📊 Académico", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            )
                        }
                    }
                }

                // ====================================================
                // TAB 0: ⏳ PENDIENTES & RECORDATORIO WHATSAPP
                // ====================================================
                if (selectedParentTab == 0) {
                    // Recordatorio con diseño estilo WhatsApp para Pago de Pensión (1ro al 5to día)
                    pendingPensionObligation?.let { pensionObligation ->
                        item {
                            WhatsAppPensionReminderCard(
                                obligation = pensionObligation,
                                onNavigateToChecklist = { selectedParentTab = 0 },
                                onDismiss = {}
                            )
                        }
                    }

                    if (pendingObligations.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.2.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎉", fontSize = 38.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "¡Todo al Día!",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = Color(0xFF15803D)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No tienes mensajes de pensión ni obligaciones pendientes por realizar este mes. ¡Excelente puntualidad familiar!",
                                        fontSize = 12.sp,
                                        color = Color(0xFF166534),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(pendingObligations, key = { it.id }) { obligation ->
                            ParentObligationCard(
                                obligation = obligation,
                                onCompleteClick = { obligationToComplete = obligation }
                            )
                        }
                    }
                }

                // ====================================================
                // TAB 1: ✅ CUMPLIDAS & MEDALLAS FAMILIARES
                // ====================================================
                if (selectedParentTab == 1) {
                    if (completedObligationsList.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📋", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Sin obligaciones cumplidas aún", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "Cuando pagues la pensión a tiempo o cumplas con un deber escolar, se guardará en este historial con su medalla de honor.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(completedObligationsList, key = { it.id }) { obligation ->
                            ParentObligationCard(
                                obligation = obligation,
                                onCompleteClick = {}
                            )
                        }
                    }

                    // Medallas & Logros Familiares
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎖️ Medallas de Familia & Mérito",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reconocimientos ganados por puntualidad y apoyo escolar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (familyBadges.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFAF5FF),
                                border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("👨‍👩‍👧", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "¡Gana medallas familiares con tu hijo/a!",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFF6B21A8)
                                        )
                                        Text(
                                            text = "Paga la pensión escolar oportunamente en los primeros 5 días del mes para recibir la insignia oficial y créditos para tu hijo/a.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF7E22CE)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(familyBadges, key = { it.id }) { badge ->
                            ParentFamilyBadgeCard(badge = badge, onClick = { selectedBadgeDetail = badge })
                        }
                    }
                }

                // ====================================================
                // TAB 2: 📊 SEGUIMIENTO ACADÉMICO DEL HIJO/A
                // ====================================================
                if (selectedParentTab == 2) {
                    // Tarjeta de Ceder / Transferir Puntos
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = GoldStar.copy(alpha = 0.12f),
                            border = BorderStroke(1.2.dp, GoldStar.copy(alpha = 0.4f)),
                            shadowElevation = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🎁", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Incentivos y Escolaris Familiares",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "Tienes 🪙 $parentBalance Escolaris para premiar a ${student?.name ?: "tu hijo/a"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Button(
                                    onClick = { showTransferPointsDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Ceder Escolaris 🚀", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    // Resumen de Métricas Clave
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // GPA Gauge (1.0 to 5.0)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Promedio GPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (gradedExams.isNotEmpty()) "${String.format(Locale.US, "%.1f", averageGrade)}/5.0" else "S/C",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(if (gradedExams.isNotEmpty()) "Escala 1.0 a 5.0" else "Sin evaluaciones", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                                }
                            }

                            // Task Progress
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Tareas al Día", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$taskCompletionPercentage%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text("$completedTasksCount de ${studentTasks.size} entregadas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            // Attendance / Tardies
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Retardos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${studentTardies.size}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (studentTardies.isEmpty()) SuccessGreen else StreakOrange
                                    )
                                    Text(if (studentTardies.isEmpty()) "Sin novedades" else "Registrados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }

                    // REPORTES DISCIPLINARIOS Y MULTAS AVISO A PADRES
                    if (studentPenalties.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = DangerRed.copy(alpha = 0.08f),
                                border = BorderStroke(1.2.dp, DangerRed.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("🚨", fontSize = 20.sp)
                                        Text(
                                            text = "Reportes Disciplinarios & Sanciones",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = DangerRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Notificaciones de sanciones registradas por el cuerpo docente:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                    studentPenalties.forEach { penalty ->
                                        val isRevoked = penalty.status == "REVOCADA"
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, if (isRevoked) MaterialTheme.colorScheme.outlineVariant else DangerRed.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = penalty.reason,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isRevoked) MaterialTheme.colorScheme.outline else DangerRed
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isRevoked) MaterialTheme.colorScheme.surfaceVariant else DangerRed.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = if (isRevoked) "Anulada" else "-${penalty.pointsDeducted} 🪙 pts",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = if (isRevoked) MaterialTheme.colorScheme.outline else DangerRed,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                if (penalty.observation.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Obs: \"${penalty.observation}\"",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Docente: ${penalty.teacherName} • ${dateFormat.format(Date(penalty.timestamp))}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Control de Asistencia & Puntualidad
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Control de Asistencia & Puntualidad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (studentTardies.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Asistencia Impecable", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("El estudiante no registra llegadas tarde en el periodo escolar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    } else {
                        items(studentTardies, key = { it.id }) { tardy ->
                            ParentTardyItemCard(tardy = tardy)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Diálogo para Vincular Código de Estudiante
    if (showLinkStudentDialog) {
        var inputCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLinkStudentDialog = false },
            title = { Text("Vincular Código de Estudiante", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ingresa el código del perfil de tu hijo/a para vincular su cuenta.",
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
                        viewModel.linkParentToStudent(inputCode)
                        showLinkStudentDialog = false
                    }
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

    // Diálogo para Ceder / Transferir Escolaris al Hijo/a
    if (showTransferPointsDialog && student != null) {
        var selectedAmountStr by remember { mutableStateOf("25") }
        var reasonText by remember { mutableStateOf("¡Excelente desempeño escolar y apoyo en casa!") }

        AlertDialog(
            onDismissRequest = { showTransferPointsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ceder Escolaris a ${student.name}", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Saldo disponible: 🪙 $parentBalance Escolaris",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )

                    Text("Selecciona una cantidad rápida a transferir:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("10", "25", "50", "100").forEach { amt ->
                            val isSelected = selectedAmountStr == amt
                            Button(
                                onClick = { selectedAmountStr = amt },
                                colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black) else ButtonDefaults.filledTonalButtonColors(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$amt 🪙", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = selectedAmountStr,
                        onValueChange = { selectedAmountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Cantidad personalizada (🪙 Escolaris)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Mensaje o motivo de reconocimiento") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val amt = selectedAmountStr.toIntOrNull() ?: 0
                val canSubmit = amt in 1..parentBalance
                Button(
                    onClick = {
                        if (amt > 0) {
                            viewModel.transferParentPointsToChild(amt, reasonText)
                            showTransferPointsDialog = false
                        }
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (amt <= 0) "Ingresa cantidad" else if (amt > parentBalance) "Saldo insuficiente" else "Ceder +$amt 🪙 Escolaris 🚀",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferPointsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Detalle de Medalla Familiar
    if (selectedBadgeDetail != null) {
        val badge = selectedBadgeDetail!!
        val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM yyyy", Locale("es", "ES"))
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(badge.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(badge.title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Text("Medalla Familiar Compartida 👨‍👩‍👧", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7C3AED))
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(badge.description, style = MaterialTheme.typography.bodyMedium)

                    if (badge.teacherNote.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFAF5FF),
                            border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("💬 Mensaje del Docente:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B21A8))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("\"${badge.teacherNote}\"", style = MaterialTheme.typography.bodySmall, color = Color(0xFF581C87))
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("👨‍🏫 Otorgado por: ${badge.unlockedByTeacher}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("📅 Fecha: ${dateFormat.format(Date(badge.unlockedAtMillis))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("🎁 Recompensa: +${badge.xpReward} XP y +${badge.creditReward} 🪙", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBadgeDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Diálogo para Confirmar Cumplimiento de Obligación
    if (obligationToComplete != null) {
        val obl = obligationToComplete!!
        AlertDialog(
            onDismissRequest = { obligationToComplete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(obl.rewardBadgeEmoji.ifBlank { "📋" }, fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Completar Deber Escolar", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "¿Deseas marcar como cumplida '${obl.title}'?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = obl.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🎁 Recompensas directas para tu hijo/a:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF166534))
                            Text("• Medalla de Honor: ${obl.rewardBadgeEmoji} ${obl.rewardBadgeTitle}", fontSize = 11.sp, color = Color(0xFF15803D))
                            Text("• Créditos Escolaris: +${obl.rewardCredits} pts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text("• Experiencia: +${obl.rewardXp} XP", fontSize = 11.sp, color = Color(0xFF15803D))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val activeParent = currentUser ?: return@Button
                        viewModel.completeParentObligation(obl, activeParent, student)
                        celebrationObligation = obl
                        obligationToComplete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirmar y Recibir Insignia", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { obligationToComplete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Celebración por Desbloqueo de Insignia de Padre
    if (celebrationObligation != null) {
        val obl = celebrationObligation!!
        AlertDialog(
            onDismissRequest = { celebrationObligation = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎉", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡Medalla Desbloqueada!", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(2.dp, GoldStar),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(obl.rewardBadgeEmoji.ifBlank { "💳" }, fontSize = 38.sp)
                        }
                    }

                    Text(
                        text = obl.rewardBadgeTitle,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF92400E),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "¡Felicitaciones! Has cumplido '${obl.title}' puntualmente. Tu hijo/a ${student?.name ?: ""} ha recibido la medalla en su perfil y +${obl.rewardCredits} créditos Escolaris.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { celebrationObligation = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¡Excelente! Continuar 🚀", fontWeight = FontWeight.Black)
                }
            }
        )
    }
}

@Composable
fun ParentObligationCard(
    obligation: ParentObligationEntity,
    onCompleteClick: () -> Unit
) {
    val isCompleted = obligation.isCompleted
    val isPension = obligation.category == "PENSION"
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val categoryColor = when (obligation.category) {
        "PENSION" -> Color(0xFF10B981)
        "DOCUMENTATION" -> Color(0xFF3B82F6)
        "EVENT" -> Color(0xFF8B5CF6)
        else -> Color(0xFF6B7280)
    }

    val categoryLabel = when (obligation.category) {
        "PENSION" -> "💳 Pensión Escolar"
        "DOCUMENTATION" -> "📄 Documentación"
        "EVENT" -> "🏛️ Evento & Reunión"
        else -> "📋 Obligación General"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted) Color(0xFFF9FAFB) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.2.dp, if (isCompleted) Color(0xFFD1D5DB) else if (isPension) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = if (isCompleted) 0.dp else 2.dp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = categoryLabel,
                        color = categoryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                if (isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cumplida", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isPension) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "⏰ 5 Primeros Días",
                            color = Color(0xFFB45309),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = obligation.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = obligation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reward preview card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCompleted) Color(0xFFF3F4F6) else Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, if (isCompleted) Color(0xFFE5E7EB) else Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(obligation.rewardBadgeEmoji.ifBlank { "🎖️" }, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Medalla: ${obligation.rewardBadgeTitle}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color.Gray else Color(0xFF92400E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${obligation.rewardCredits} 🪙",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCompleted) Color.Gray else Color(0xFFB45309),
                        maxLines = 1
                    )
                }
            }

            if (!isCompleted) {
                Button(
                    onClick = onCompleteClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPension) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPension) "Marcar Pago como Realizado ✅" else "Marcar como Cumplida ✅",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                obligation.completedAtMillis?.let { timestamp ->
                    Text(
                        text = "Cumplida el ${dateFormat.format(Date(timestamp))} por ${obligation.completedByParentName.ifBlank { "Acudiente" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ParentFamilyBadgeCard(
    badge: BadgeEntity,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAF5FF),
        border = BorderStroke(1.2.dp, Color(0xFFD8B4FE)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEDE9FE),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(badge.emoji, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badge.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF581C87)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Compartida",
                            color = Color(0xFF7C3AED),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (badge.teacherNote.isNotBlank()) {
                    Text(
                        text = "\"${badge.teacherNote}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 Otorgada por: ${badge.unlockedByTeacher}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7E22CE)
                    )
                    Text(
                        text = "+${badge.xpReward} XP / +${badge.creditReward} pts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

@Composable
fun ParentTardyItemCard(tardy: TardyRecordEntity) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val isJustified = tardy.status == "JUSTIFICADO"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isJustified) SuccessGreen.copy(alpha = 0.4f) else StreakOrange.copy(alpha = 0.4f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = null,
                        tint = if (isJustified) SuccessGreen else StreakOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Llegada Tarde (+${tardy.delayMinutes} min)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isJustified) SuccessGreen.copy(alpha = 0.15f) else StreakOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tardy.status,
                        fontWeight = FontWeight.Bold,
                        color = if (isJustified) SuccessGreen else StreakOrange,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Materia: ${tardy.subject} • Hora llegada: ${tardy.arrivalTime} • ${dateFormat.format(Date(tardy.dateMillis))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (tardy.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Motivo: ${tardy.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (tardy.teacherObservation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💬 Observación Docente: \"${tardy.teacherObservation}\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
