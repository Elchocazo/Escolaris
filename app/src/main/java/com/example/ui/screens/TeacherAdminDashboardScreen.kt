package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.ProfilePhotoViewerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.ParentObligationEntity
import com.example.data.local.entity.RedemptionEntity
import com.example.data.local.entity.RewardEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TardyRecordEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.AttendanceStatus
import com.example.domain.model.UserRole
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherAdminDashboardScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val tardyRecords by viewModel.tardyRecords.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val redemptions by viewModel.redemptions.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val allPenalties by viewModel.allPenalties.collectAsState()
    val parentObligations by viewModel.parentObligations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolEvents by viewModel.allSchoolEvents.collectAsState()
    val context = LocalContext.current

    val clipboardManager = LocalClipboardManager.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showRecordTardyDialog by remember { mutableStateOf(false) }
    var showGrantBadgeDialog by remember { mutableStateOf(false) }
    var showAddObligationDialog by remember { mutableStateOf(false) }
    var showIssuePenaltyDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showResetPointsConfirmationDialog by remember { mutableStateOf(false) }
    var showCreateRewardDialog by remember { mutableStateOf(false) }
    var rewardToEdit by remember { mutableStateOf<RewardEntity?>(null) }
    var rewardToDelete by remember { mutableStateOf<RewardEntity?>(null) }
    var showGroupPointsDialog by remember { mutableStateOf(false) }
    var obligationToAwardPoints by remember { mutableStateOf<ParentObligationEntity?>(null) }
    var teacherSearchQuery by remember { mutableStateOf("") }
    var selectedTeacherForDetail by remember { mutableStateOf<TeacherDirectoryEntry?>(null) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var userToAdjustPoints by remember { mutableStateOf<UserEntity?>(null) }
    var examToGrade by remember { mutableStateOf<ExamEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var selectedRoleFilter by remember { mutableStateOf("TODOS") }
    var userSearchQuery by remember { mutableStateOf("") }
    var selectedUserForPhoto by remember { mutableStateOf<UserEntity?>(null) }
    var selectedSubjectDetail by remember { mutableStateOf<SubjectEntity?>(null) }

    val students = remember(allUsers) {
        allUsers.filter { it.role == UserRole.STUDENT.code }
    }

    val filteredUsers = remember(allUsers, selectedRoleFilter, userSearchQuery) {
        allUsers.filter { user ->
            val matchesRole = when (selectedRoleFilter) {
                "ESTUDIANTES" -> user.role == UserRole.STUDENT.code
                "DOCENTES" -> user.role == UserRole.TEACHER.code
                "PADRES" -> user.role == UserRole.PARENT.code
                else -> true
            }
            val matchesSearch = if (userSearchQuery.isBlank()) true else {
                user.name.contains(userSearchQuery, ignoreCase = true) ||
                user.email.contains(userSearchQuery, ignoreCase = true) ||
                user.studentCode.contains(userSearchQuery, ignoreCase = true) ||
                user.teacherCode.contains(userSearchQuery, ignoreCase = true) ||
                user.gradeSection.contains(userSearchQuery, ignoreCase = true)
            }
            matchesRole && matchesSearch
        }
    }

    val filteredTeachers = remember(teacherSearchQuery) {
        if (teacherSearchQuery.isBlank()) {
            OFFICIAL_TEACHER_DIRECTORY
        } else {
            val q = teacherSearchQuery.trim().lowercase()
            OFFICIAL_TEACHER_DIRECTORY.filter {
                it.name.lowercase().contains(q) ||
                it.subject.lowercase().contains(q) ||
                it.roleOrGrade.lowercase().contains(q) ||
                (it.email?.lowercase()?.contains(q) == true) ||
                it.attentionDay.lowercase().contains(q)
            }
        }
    }

    val tabTitles = listOf(
        "⏰ Retardos",        // 0
        "🎖️ Logros",          // 1
        "📋 Deberes Padres",  // 2
        "🚨 Multas",          // 3
        "📝 Calificar",       // 4
        "🛍️ Tienda",          // 5
        "🎟️ Canjes",          // 6
        "📚 Materias",        // 7
        "👥 Usuarios",        // 8
        "📞 Directorio",      // 9
        "📄 Resumen 7°"       // 10
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // SuperAdmin Title Bar
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "SuperAdmin Escolar",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Panel SuperAdmin Escolar",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Control total y auditoría institucional 7° Grado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            when (selectedTabIndex) {
                                0 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showRecordTardyDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("admin_add_tardy_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Retardo", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                }
                                1 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showGrantBadgeDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("admin_grant_badge_button")
                                    ) {
                                        Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Insignia", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                }
                                2 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showAddObligationDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Deber", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                }
                                3 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showIssuePenaltyDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Multar", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                }
                                5 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showCreateRewardDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("admin_add_reward_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Recompensa", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                }
                                7 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showAddSubjectDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Materia", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                }
                                8 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { showAddUserDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Persona", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                        }
                                        Button(
                                            onClick = { showGroupPointsDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Puntos Grupo", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                        }
                                    }
                                }
                                9 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            com.example.utils.PdfExporter.generateAndShareTeacherDirectory(
                                                context = context,
                                                teachers = OFFICIAL_TEACHER_DIRECTORY
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Directorio PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                                10 -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            com.example.utils.PdfExporter.generateAndShareCourseSummary(
                                                context = context,
                                                weeklySchedule = OFFICIAL_SCHEDULE_MAP,
                                                teachers = OFFICIAL_TEACHER_DIRECTORY,
                                                events = schoolEvents
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Descargar PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        }
                    }

                    // Teacher Classroom Code Banner & KPI Row - ONLY shown on the first tab
                    val teacherClassCode = currentUser?.teacherCode?.ifBlank { "DOC-102938" } ?: "DOC-102938"

                    if (selectedTabIndex == 0) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Teacher Classroom Code Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🏫 CÓDIGO DE TU AULA / CLASE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = teacherClassCode,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Comparte este código para que tus alumnos se unan a tu aula.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(teacherClassCode))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar Código",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Institutional KPI Cards Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val cardData = listOf(
                                Triple("🎓 Alumnos", "${allUsers.count { it.role == UserRole.STUDENT.code }}", MaterialTheme.colorScheme.primary),
                                Triple("👨‍🏫 Docentes", "${allUsers.count { it.role == UserRole.TEACHER.code }}", SuccessGreen),
                                Triple("👨‍👩‍👧 Padres", "${allUsers.count { it.role == UserRole.PARENT.code }}", Color(0xFF7C3AED)),
                                Triple("🎖️ Logros", "${allBadges.size}", GoldStar)
                            )
                            cardData.forEach { (label, count, color) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = color.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = count,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = color
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        // TARDY RECORDS TAB
                        if (tardyRecords.isEmpty()) {
                            item {
                                AdminEmptyCard("No hay registros de llegadas tarde.")
                            }
                        } else {
                            items(tardyRecords, key = { it.id }) { record ->
                                AdminTardyCard(
                                    record = record,
                                    onJustify = {
                                        viewModel.updateTardyStatus(record.id, AttendanceStatus.JUSTIFICADO.code, "Justificante verificado por docente titular.")
                                    },
                                    onDelete = { viewModel.deleteTardyRecord(record) }
                                )
                            }
                        }
                    }
                    1 -> {
                        // BADGES TAB
                        if (allBadges.isEmpty()) {
                            item {
                                AdminEmptyCard("No se han otorgado insignias de honor aún.")
                            }
                        } else {
                            items(allBadges, key = { it.id }) { badge ->
                                AdminBadgeCard(
                                    badge = badge,
                                    onRevoke = { viewModel.revokeBadge(badge) }
                                )
                            }
                        }
                    }
                    2 -> {
                        // PARENT OBLIGATIONS & PENSIONES TAB
                        if (parentObligations.isEmpty()) {
                            item {
                                AdminEmptyCard("No hay deberes ni pagos de pensión asignados a los padres.")
                            }
                        } else {
                            items(parentObligations, key = { it.id }) { obligation ->
                                AdminParentObligationCard(
                                    obligation = obligation,
                                    onBroadcastReminder = {
                                        viewModel.sendWhatsAppReminderToParents(obligation)
                                    },
                                    onAwardAttendance = {
                                        obligationToAwardPoints = obligation
                                    },
                                    onDelete = { viewModel.deleteParentObligation(obligation) }
                                )
                            }
                        }
                    }
                    3 -> {
                        // DISCIPLINARY PENALTIES / MULTAS TAB
                        if (allPenalties.isEmpty()) {
                            item {
                                AdminEmptyCard("No hay sanciones disciplinarias ni multas registradas.")
                            }
                        } else {
                            items(allPenalties, key = { it.id }) { penalty ->
                                AdminPenaltyCard(
                                    penalty = penalty,
                                    onRevoke = { viewModel.revokePenalty(penalty) }
                                )
                            }
                        }
                    }
                    4 -> {
                        // GRADING (1.0 TO 5.0) TAB
                        if (exams.isEmpty()) {
                            item {
                                AdminEmptyCard("No hay evaluaciones ni exámenes pendientes por calificar.")
                            }
                        } else {
                            items(exams, key = { it.id }) { exam ->
                                AdminExamGradeCard(
                                    exam = exam,
                                    onGradeClick = { examToGrade = exam }
                                )
                            }
                        }
                    }
                    5 -> {
                        // REWARDS STORE / TIENDA TAB (CATÁLOGO ESCOLAR)
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🛍️ Catálogo y Tienda Escolar",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Total de artículos: ${rewards.size} | Publica recompensas, existencias y precios en 🪙 Escolaris",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { showCreateRewardDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Crear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (rewards.isEmpty()) {
                            item {
                                AdminEmptyCard("No hay artículos en la tienda escolar. Haz clic en 'Crear' para publicar la primera recompensa.")
                            }
                        } else {
                            items(rewards, key = { it.id }) { reward ->
                                RewardItemCard(
                                    reward = reward,
                                    canAfford = true,
                                    isTeacher = true,
                                    isParent = false,
                                    onRedeem = {},
                                    onEdit = { rewardToEdit = reward },
                                    onDelete = { rewardToDelete = reward }
                                )
                            }
                        }
                    }
                    6 -> {
                        // PASSES / REDEMPTIONS TAB (CANJES)
                        if (redemptions.isEmpty()) {
                            item {
                                AdminEmptyCard("No hay canjes de recompensas solicitados por estudiantes.")
                            }
                        } else {
                            items(redemptions, key = { it.id }) { redemption ->
                                AdminRedemptionCard(
                                    redemption = redemption,
                                    onApprove = { viewModel.approveStudentRedemption(redemption.id) },
                                    onReject = { viewModel.rejectStudentRedemption(redemption.id) },
                                    onValidate = { viewModel.validateStudentRedemption(redemption.id) }
                                )
                            }
                        }
                    }
                    7 -> {
                        // SUBJECTS / ASIGNATURAS TAB
                        if (allSubjects.isEmpty()) {
                            item {
                                AdminEmptyCard("No hay asignaturas escolares configuradas en el sistema.")
                            }
                        } else {
                            items(allSubjects, key = { it.id }) { subject ->
                                AdminSubjectCard(
                                    subject = subject,
                                    onDelete = { viewModel.deleteSubject(subject) },
                                    onClick = { selectedSubjectDetail = subject }
                                )
                            }
                        }
                    }
                    8 -> {
                        // USERS MANAGEMENT TAB (BORRAR / GESTIONAR USUARIOS)
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = userSearchQuery,
                                    onValueChange = { userSearchQuery = it },
                                    leadingIcon = { Icon(Icons.Default.Search, null) },
                                    trailingIcon = {
                                        if (userSearchQuery.isNotBlank()) {
                                            IconButton(onClick = { userSearchQuery = "" }) {
                                                Icon(Icons.Default.Close, "Limpiar búsqueda")
                                            }
                                        }
                                    },
                                    placeholder = { Text("Buscar por nombre, correo o código...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                val roleOptions = listOf(
                                    "TODOS" to "Todos (${allUsers.size})",
                                    "ESTUDIANTES" to "Estudiantes (${allUsers.count { it.role == UserRole.STUDENT.code }})",
                                    "DOCENTES" to "Docentes (${allUsers.count { it.role == UserRole.TEACHER.code }})",
                                    "PADRES" to "Padres (${allUsers.count { it.role == UserRole.PARENT.code }})"
                                )

                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    roleOptions.forEach { (roleCode, roleLabel) ->
                                        val isSelected = selectedRoleFilter == roleCode
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { selectedRoleFilter = roleCode }
                                        ) {
                                            Text(
                                                text = roleLabel,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Botón de Restablecer Puntos Oficiales
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DangerRed.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🔄 Restablecer Puntos Oficiales",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = DangerRed
                                            )
                                            Text(
                                                text = "Restaura todos a 100 🪙 Escolaris y 50 XP por defecto.",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = { showResetPointsConfirmationDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Restablecer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🪙 Otorgar Puntos en Grupo",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFB45309)
                                            )
                                            Text(
                                                text = "Selecciona múltiples personas para asignar créditos y XP juntos.",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = { showGroupPointsDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Seleccionar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredUsers.isEmpty()) {
                            item {
                                AdminEmptyCard("No se encontraron usuarios registrados con los filtros seleccionados.")
                            }
                        } else {
                            items(filteredUsers, key = { it.id }) { user ->
                                AdminUserRowCard(
                                    user = user,
                                    isCurrentUser = user.id == currentUser?.id,
                                    onPhotoClick = { selectedUserForPhoto = user },
                                    onEditClick = { userToEdit = user },
                                    onAdjustPointsClick = { userToAdjustPoints = user },
                                    onDeleteClick = { userToDelete = user }
                                )
                            }
                        }
                    }
                    9 -> {
                        // FACULTY DIRECTORY TAB (DIRECTORIO DOCENTE EN PANEL ADMIN)
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                                            Text("🏫", fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Directorio de Docentes & Directivos",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${OFFICIAL_TEACHER_DIRECTORY.size} miembros institucionales • 7° Grado",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                com.example.utils.PdfExporter.generateAndShareTeacherDirectory(context, OFFICIAL_TEACHER_DIRECTORY)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PDF", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedTextField(
                                        value = teacherSearchQuery,
                                        onValueChange = { teacherSearchQuery = it },
                                        placeholder = { Text("Buscar por docente, materia o día...", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                                        trailingIcon = {
                                            if (teacherSearchQuery.isNotBlank()) {
                                                IconButton(onClick = { teacherSearchQuery = "" }) {
                                                    Icon(Icons.Default.Close, null)
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        if (filteredTeachers.isEmpty()) {
                            item {
                                AdminEmptyCard("No se encontraron docentes con el criterio de búsqueda.")
                            }
                        } else {
                            items(filteredTeachers, key = { it.name }) { teacher ->
                                AdminTeacherDirectoryCard(
                                    teacher = teacher,
                                    onClick = { selectedTeacherForDetail = teacher }
                                )
                            }
                        }
                    }
                    10 -> {
                        // COURSE SUMMARY TAB (DESCARGA RESUMEN)
                        item {
                            AdminCourseSummarySection(
                                totalStudents = students.size,
                                totalSubjects = allSubjects.size,
                                teachersCount = OFFICIAL_TEACHER_DIRECTORY.size,
                                eventsCount = schoolEvents.size,
                                onDownloadPdf = {
                                    com.example.utils.PdfExporter.generateAndShareCourseSummary(
                                        context = context,
                                        weeklySchedule = OFFICIAL_SCHEDULE_MAP,
                                        teachers = OFFICIAL_TEACHER_DIRECTORY,
                                        events = schoolEvents
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddObligationDialog) {
        AddParentObligationDialog(
            onDismiss = { showAddObligationDialog = false },
            onAdd = { title, desc, cat, month, dueDay, badgeKey, badgeTitle, badgeEmoji, credits, xp, waMsg ->
                viewModel.addParentObligation(
                    title = title,
                    description = desc,
                    category = cat,
                    month = month,
                    dueDayOfMonth = dueDay,
                    dueDateMillis = System.currentTimeMillis() + (dueDay * 24L * 3600L * 1000L),
                    rewardBadgeKey = badgeKey,
                    rewardBadgeTitle = badgeTitle,
                    rewardBadgeEmoji = badgeEmoji,
                    rewardCredits = credits,
                    rewardXp = xp,
                    whatsappMessage = waMsg
                )
            }
        )
    }

    if (showIssuePenaltyDialog) {
        IssuePenaltyDialog(
            students = students,
            onDismiss = { showIssuePenaltyDialog = false },
            onIssue = { sId, sName, reason, cat, pts, obs, notify ->
                viewModel.issueStudentPenalty(sId, sName, reason, cat, pts, obs, notify)
            }
        )
    }

    if (showAddUserDialog) {
        AdminAddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onAdd = { name, email, role, grade, credits, xp ->
                viewModel.adminCreateUser(name, email, role, grade, credits, xp)
            }
        )
    }

    userToEdit?.let { target ->
        AdminEditUserDialog(
            user = target,
            onDismiss = { userToEdit = null },
            onSave = { name, email, role, grade, credits, xp, code ->
                viewModel.adminUpdateUser(target.id, name, email, role, grade, credits, xp, code)
            }
        )
    }

    userToAdjustPoints?.let { target ->
        AdminAdjustPointsDialog(
            user = target,
            onDismiss = { userToAdjustPoints = null },
            onAdjust = { deltaCredits, deltaXp, reason ->
                viewModel.adminAdjustUserCredits(target.id, deltaCredits, deltaXp, reason)
            }
        )
    }

    if (userToDelete != null) {
        val target = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("¿Eliminar usuario del sistema?", fontWeight = FontWeight.Bold, color = DangerRed) },
            text = {
                Column {
                    Text("¿Estás seguro de que deseas eliminar permanentemente la cuenta de:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("👤 ${target.name}", fontWeight = FontWeight.Bold)
                            Text("🏷️ Rol: ${target.role} • 📧 ${target.email}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                            if (target.studentCode.isNotBlank()) {
                                Text("🆔 Código: ${target.studentCode}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Esta acción no se puede deshacer. Se borrará toda su información de la base de datos.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(target)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Eliminar Definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showGroupPointsDialog) {
        AdminGroupAdjustPointsDialog(
            allUsers = allUsers,
            onDismiss = { showGroupPointsDialog = false },
            onConfirm = { targetIds, deltaCredits, deltaXp, reason ->
                viewModel.adminAdjustMultipleUsersCredits(targetIds, deltaCredits, deltaXp, reason)
                showGroupPointsDialog = false
            }
        )
    }

    obligationToAwardPoints?.let { obl ->
        AdminObligationAttendanceDialog(
            obligation = obl,
            allUsers = allUsers,
            onDismiss = { obligationToAwardPoints = null },
            onConfirm = { selectedIds ->
                viewModel.adminAdjustMultipleUsersCredits(
                    targetUserIds = selectedIds,
                    deltaCredits = obl.rewardCredits,
                    deltaXp = obl.rewardXp,
                    reason = "Cumplimiento: ${obl.title}"
                )
                if (obl.rewardBadgeTitle.isNotBlank()) {
                    selectedIds.forEach { uId ->
                        val u = allUsers.find { it.id == uId }
                        if (u != null) {
                            viewModel.unlockBadgeForStudent(
                                studentId = u.id,
                                studentName = u.name,
                                badgeKey = obl.rewardBadgeKey.ifBlank { "OBLIGATION_${obl.id}" },
                                title = obl.rewardBadgeTitle,
                                description = "Reconocimiento por asistencia y cumplimiento en: ${obl.title}",
                                emoji = obl.rewardBadgeEmoji.ifBlank { "🎖️" },
                                category = "FAMILIA",
                                teacherNote = "Validado en panel docente",
                                xpReward = 0,
                                creditReward = 0
                            )
                        }
                    }
                }
                obligationToAwardPoints = null
            }
        )
    }

    selectedTeacherForDetail?.let { teacher ->
        TeacherDetailDialog(
            teacher = teacher,
            onDismiss = { selectedTeacherForDetail = null }
        )
    }

    if (selectedUserForPhoto != null) {
        val target = selectedUserForPhoto!!
        ProfilePhotoViewerDialog(
            photoUri = target.photoUri,
            avatarEmoji = target.avatarEmoji,
            userName = target.name,
            userRole = target.role,
            gradeSection = target.gradeSection,
            onDismiss = { selectedUserForPhoto = null }
        )
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onAdd = { name, emoji, teacherName, classroom, colorHex, desc ->
                viewModel.addSubject(name, emoji, teacherName, classroom, colorHex, desc)
            }
        )
    }

    selectedSubjectDetail?.let { sub ->
        AlertDialog(
            onDismissRequest = { selectedSubjectDetail = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(sub.colorHex).copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(sub.emoji, fontSize = 26.sp)
                        }
                    }
                    Column {
                        Text(
                            text = sub.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (sub.classroom.isNotBlank()) "Salón ${sub.classroom} • 7° Grado" else "Plan de Estudios • 7° Grado",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👨‍🏫", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Docente Asignado:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1E40AF),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = sub.teacherName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Materia Oficial:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = sub.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (sub.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = sub.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedSubjectDetail = null },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showRecordTardyDialog) {
        RecordTardyDialog(
            students = students,
            subjects = allSubjects,
            onDismiss = { showRecordTardyDialog = false },
            onRecord = { studentId, studentName, delayMin, subject, reason, arrivalTime, notifyParents, obs ->
                viewModel.recordTardyArrival(
                    studentId = studentId,
                    studentName = studentName,
                    delayMinutes = delayMin,
                    subject = subject,
                    reason = reason,
                    arrivalTime = arrivalTime,
                    notifyParents = notifyParents,
                    observation = obs
                )
            }
        )
    }

    if (showGrantBadgeDialog) {
        GrantBadgeDialog(
            students = students,
            allUsers = allUsers,
            onDismiss = { showGrantBadgeDialog = false },
            onGrantMultiple = { targetUserIds, key, title, desc, emoji, category, note, xp, credits, photoUri ->
                viewModel.grantBadgeToMultipleUsers(
                    targetUserIds = targetUserIds,
                    badgeKey = key,
                    badgeTitle = title,
                    badgeDesc = desc,
                    badgeEmoji = emoji,
                    category = category,
                    teacherNote = note,
                    xpReward = xp,
                    creditReward = credits,
                    photoUri = photoUri
                )
            }
        )
    }

    if (showResetPointsConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showResetPointsConfirmationDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
                    Text("¿Restablecer Puntos Oficiales?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Esta acción reiniciará los puntos y créditos Escolaris de todos los estudiantes y acudientes a sus valores oficiales por defecto:\n\n• Estudiantes: 100 🪙 Escolaris / 50 XP\n• Acudientes: 100 🪙 Escolaris / 100 Incentivos / 50 XP\n\nSe sincronizará tanto en este dispositivo local como en la nube oficial de Google Cloud Firestore.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllUsersPointsToDefault()
                        showResetPointsConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sí, Restablecer Puntos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPointsConfirmationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    examToGrade?.let { exam ->
        GradeExamDialog(
            exam = exam,
            onDismiss = { examToGrade = null },
            onSaveGrade = { grade, feedback ->
                viewModel.gradeStudentExam(exam.id, grade, feedback)
            }
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
}

@Composable
fun AdminEmptyCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📋", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun AdminTardyCard(
    record: TardyRecordEntity,
    onJustify: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val isJustified = record.status == AttendanceStatus.JUSTIFICADO.code

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("admin_tardy_card_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${record.studentName} (+${record.delayMinutes} min)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${record.subject} • Llegada: ${record.arrivalTime} (${dateFormat.format(Date(record.dateMillis))})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isJustified) SuccessGreen.copy(alpha = 0.15f) else StreakOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = record.status,
                        fontWeight = FontWeight.Bold,
                        color = if (isJustified) SuccessGreen else StreakOrange,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Motivo: ${record.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isJustified) {
                    Button(
                        onClick = onJustify,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_justify_button_${record.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar Justificante", fontSize = 11.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AdminBadgeCard(
    badge: BadgeEntity,
    onRevoke: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("admin_badge_card_${badge.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (badge.category == "FAMILY") Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(badge.clayColorHex).copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (badge.category == "FAMILY") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "👨‍👩‍👧 Familia",
                                color = Color(0xFF7C3AED),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (badge.teacherNote.isNotBlank()) "\"${badge.teacherNote}\"" else badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("+${badge.xpReward} XP", style = MaterialTheme.typography.labelSmall, color = GoldStar, fontWeight = FontWeight.Bold)
                    Text("+${badge.creditReward} Pts", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(onClick = onRevoke, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Revocar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AdminExamGradeCard(
    exam: ExamEntity,
    onGradeClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${exam.subject} • ${exam.studentId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (exam.isGraded && exam.grade != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nota: ${String.format(Locale.US, "%.1f", exam.grade)} / 5.0",
                        fontWeight = FontWeight.Bold,
                        color = if (exam.grade >= 3.0) SuccessGreen else DangerRed,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Button(
                onClick = onGradeClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("grade_exam_button_${exam.id}")
            ) {
                Icon(Icons.Default.Grade, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (exam.isGraded) "Editar Nota" else "Calificar (1.0-5.0)", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AdminRedemptionCard(
    redemption: RedemptionEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onValidate: () -> Unit
) {
    val isPending = redemption.status == "PENDING_APPROVAL" || redemption.status == "PENDIENTE"
    val isApproved = redemption.status == "APROBADO" || redemption.status == "ACTIVO"
    val isUsed = redemption.status == "UTILIZADO"
    val isRejected = redemption.status == "RECHAZADO"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = redemption.rewardTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Alumno: ${redemption.studentName} • ${redemption.costCredits} 🪙 • Código: ${redemption.redemptionCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isApproved -> SuccessGreen.copy(alpha = 0.15f)
                        isPending -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        isUsed -> MaterialTheme.colorScheme.surfaceVariant
                        else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when {
                            isApproved -> "🎟️ Aprobado"
                            isPending -> "⏳ Pendiente"
                            isUsed -> "✅ Utilizado"
                            else -> "❌ Rechazado"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isApproved -> SuccessGreen
                            isPending -> Color(0xFFD97706)
                            isUsed -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> Color(0xFFDC2626)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isPending -> {
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("❌ Rechazar (Devolver)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("✅ Aceptar Canje", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    isApproved -> {
                        Button(
                            onClick = onValidate,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("🎟️ Usar en Clase", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    isUsed -> {
                        Text("Pase redimido en el aula", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    isRejected -> {
                        Text("Canje rechazado (Puntos devueltos)", fontSize = 11.sp, color = Color(0xFFDC2626))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordTardyDialog(
    students: List<UserEntity>,
    subjects: List<SubjectEntity> = emptyList(),
    onDismiss: () -> Unit,
    onRecord: (studentId: String, studentName: String, delayMin: Int, subject: String, reason: String, arrivalTime: String, notifyParents: Boolean, obs: String) -> Unit
) {
    val subjectNames = if (subjects.isNotEmpty()) subjects.map { it.name }.distinct() else com.example.domain.model.OFFICIAL_7TH_GRADE_SUBJECTS

    var manualStudentName by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var delayMinutesStr by remember { mutableStateOf("15") }
    var selectedSubject by remember { mutableStateOf(subjectNames.firstOrNull() ?: "Matemáticas") }
    var reason by remember { mutableStateOf("Tráfico pesado") }
    var arrivalTime by remember { mutableStateOf("07:45 AM") }
    var notifyParents by remember { mutableStateOf(true) }
    var observation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Retardo", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (students.isNotEmpty()) {
                    Text("Selecciona el Estudiante:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        students.forEach { s ->
                            val isSelected = selectedStudent?.id == s.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedStudent = s }
                            ) {
                                Text(
                                    text = s.name,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = manualStudentName,
                        onValueChange = { manualStudentName = it },
                        label = { Text("Nombre del Estudiante") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = delayMinutesStr,
                        onValueChange = { delayMinutesStr = it },
                        label = { Text("Minutos de retraso") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = arrivalTime,
                        onValueChange = { arrivalTime = it },
                        label = { Text("Hora llegada") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text("Materia de la clase:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjectNames.forEach { sub ->
                        val isSelected = selectedSubject == sub
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedSubject = sub }
                        ) {
                            Text(
                                text = sub,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo del retardo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyParents, onCheckedChange = { notifyParents = it })
                    Text("Enviar alerta inmediata a los padres 📲", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sId = selectedStudent?.id ?: "manual_${System.currentTimeMillis()}"
                    val sName = selectedStudent?.name ?: manualStudentName.ifBlank { "Estudiante" }
                    val mins = delayMinutesStr.toIntOrNull() ?: 10
                    onRecord(sId, sName, mins, selectedSubject, reason, arrivalTime, notifyParents, observation)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_record_tardy_button")
            ) {
                Text("Registrar Retardo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDropdownField(
    students: List<UserEntity>,
    selectedStudent: UserEntity?,
    onStudentSelected: (UserEntity) -> Unit,
    label: String = "Estudiante",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedStudent?.let { "${it.avatarEmoji.ifBlank { "🎓" }} ${it.name} (${it.gradeSection.ifBlank { "Estudiante" }})" } ?: "Seleccionar estudiante...",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontWeight = FontWeight.Bold) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (students.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay estudiantes registrados") },
                    onClick = { expanded = false }
                )
            } else {
                students.forEach { s ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(s.avatarEmoji.ifBlank { "🎓" }, fontSize = 18.sp)
                                    Column {
                                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${s.gradeSection} ${if (s.studentCode.isNotBlank()) "• ${s.studentCode}" else ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text("🪙 ${s.credits}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        },
                        onClick = {
                            onStudentSelected(s)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrantBadgeDialog(
    students: List<UserEntity>,
    allUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onGrantMultiple: (targetUserIds: List<String>, key: String, title: String, desc: String, emoji: String, category: String, note: String, xp: Int, credits: Int, photoUri: String?) -> Unit
) {
    var creationMode by remember { mutableStateOf("PRESET") } // "PRESET" or "CUSTOM"
    var selectedBadgeType by remember { mutableStateOf("FAMILY") } // "FAMILY" or "STUDENT"
    var badgePhotoUri by remember { mutableStateOf<String?>(null) }

    // Custom badge form fields
    var customTitle by remember { mutableStateOf("") }
    var customEmoji by remember { mutableStateOf("⭐") }
    var customDesc by remember { mutableStateOf("") }
    var customCredits by remember { mutableIntStateOf(100) }
    var customXp by remember { mutableIntStateOf(100) }

    // Recipient selection state
    val parents = remember(allUsers) { allUsers.filter { it.role == UserRole.PARENT.code } }
    var recipientRole by remember { mutableStateOf("PARENTS") }
    var recipientSearchQuery by remember { mutableStateOf("") }
    var selectedUserIds by remember { mutableStateOf(setOf<String>()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        badgePhotoUri = uri?.toString()
    }

    data class BadgeOption(
        val key: String,
        val emoji: String,
        val title: String,
        val desc: String,
        val credits: Int,
        val xp: Int,
        val defaultNote: String
    )

    val studentPresetBadges = listOf(
        BadgeOption("FLAG_RAISING", "🇨🇴", "Izó Bandera", "Honor patrio, convivencia ejemplar y civismo institucional.", 50, 100, "¡Felicitaciones por portar el pabellón patrio con honor y excelencia!"),
        BadgeOption("FIRST_GRADE_5", "⭐", "Examen 5.0", "Calificación perfecta y excelencia académica en evaluación.", 60, 120, "¡Excelente desempeño y puntaje perfecto 5.0 en la evaluación!"),
        BadgeOption("STREAK_7_DAYS", "🔥", "Racha Imparable", "Disciplina constante, puntualidad y tareas al día toda la semana.", 50, 100, "¡Constancia impecable y disciplina diaria durante toda la semana!"),
        BadgeOption("STEAM_CREATIVITY", "🔬", "Científico STEAM", "Diseño, innovación e investigación destacada en proyectos prácticos.", 75, 150, "¡Gran creatividad y aporte investigativo en proyectos STEAM!"),
        BadgeOption("PEER_HELPER", "🤝", "Líder Colaborativo", "Apoyo desinteresado a compañeros y trabajo solidario en equipo.", 50, 100, "¡Gracias por tu compañerismo y apoyo constante a tus compañeros!"),
        BadgeOption("EUREKA_GENIUS", "🧠", "Genio Resolutivo / Eureka", "Resolución brillante e ingeniosa de problemas complejos.", 60, 120, "¡Mente brillante y excelente capacidad para resolver desafíos!"),
        BadgeOption("GUARDIAN_RESPECT", "🛡️", "Guardián del Respeto", "Promotor activo de la convivencia, paz escolar y buen trato.", 50, 100, "¡Ejemplo de respeto, tolerancia y sana convivencia escolar!"),
        BadgeOption("PERFECT_ATTENDANCE", "⏰", "Puntualidad de Oro", "Asistencia impecable y cero retardos en el periodo.", 50, 100, "¡Puntualidad intachable y gran compromiso con las clases!"),
        BadgeOption("BOOK_DEVOURER", "📚", "Devorador de Libros", "Compromiso sobresaliente con la lectura y comprensión crítica.", 50, 100, "¡Fascinante hábito lector y análisis crítico en clase!"),
        BadgeOption("ARTISTIC_EXPRESSION", "🎨", "Creatividad sin Límites", "Originalidad y talento sobresaliente en artes y diseño.", 50, 100, "¡Impresionante talento artístico y expresión creativa!"),
        BadgeOption("ECO_HERO", "🌱", "Eco-Héroe Escolar", "Cuidado del medio ambiente y preservación del aula limpia.", 50, 100, "¡Compromiso ejemplar con el cuidado del medio ambiente y el entorno!"),
        BadgeOption("PERSONAL_GROWTH", "📈", "Superación & Perseverancia", "Gran progreso personal y esfuerzo académico notable.", 60, 120, "¡Gran avance y esfuerzo constante para superarte día a día!"),
        BadgeOption("CURIOUS_MIND", "💡", "Mente Curiosa & Proactiva", "Participación activa con aportes y preguntas de gran valor.", 50, 100, "¡Gran iniciativa, preguntas brillantes y actitud proactiva en clase!")
    )

    val familyPresetBadges = listOf(
        BadgeOption("PARENT_MEETING_1", "👨‍👩‍👧", "Asistencia a 1ª Reunión de Padres", "Asistencia puntual y compromiso activo en la primera reunión escolar institucional.", 100, 150, "¡Agradecemos su valiosa asistencia y compromiso en la primera reunión de padres del año!"),
        BadgeOption("PARENT_PENSION_OCTUBRE", "💳", "Pago Oportuno de Pensión", "Cancelación oportuna de la pensión escolar en los 5 primeros días del mes.", 100, 150, "¡Agradecemos su valioso compromiso y puntualidad en el pago oportuno de la pensión escolar!"),
        BadgeOption("PARENT_PENSION_SEMESTER", "💎", "Pensión al Día - Semestre", "Cumplimiento impecable y mensual de las obligaciones económicas escolares.", 150, 250, "¡Reconocimiento de honor por mantener su pensión escolar al día mes a mes!"),
        BadgeOption("PARENT_EXEMPLARY_TUTOR", "👑", "Tutor Ejemplar & Puntual", "Acompañamiento integral, puntualidad y constante apoyo formativo en el hogar.", 120, 200, "¡Homenaje a un acudiente ejemplar en la formación y disciplina de su hijo/a!"),
        BadgeOption("PARENT_COLLABORATOR", "🤝", "Padre Colaborador", "Participación activa y colaboración en actividades escolares.", 80, 150, "¡Gracias por su apoyo activo en las actividades pedagógicas de la institución!"),
        BadgeOption("FAMILY_EXCELLENCE", "🏆", "Familia Ejemplar", "Acompañamiento formativo, disciplina y apoyo educativo en casa.", 100, 200, "¡Reconocimiento especial a una familia comprometida con los valores y la formación!"),
        BadgeOption("PUNCTUAL_FAMILY", "⏰", "Familia Puntual", "Puntualidad intachable y cero retardos escolares en el periodo.", 80, 150, "¡Felicitaciones a la familia por su puntualidad y responsabilidad intachable!"),
        BadgeOption("READING_AT_HOME", "📖", "Lectura en Familia", "Fomento de la lectura compartida y hábito lector en el hogar.", 80, 150, "¡Gran labor fomentando la lectura compartida y el amor por los libros en el hogar!"),
        BadgeOption("HOMEWORK_SUPPORT", "✍️", "Apoyo en Tareas", "Acompañamiento positivo en el cumplimiento de deberes escolares.", 80, 150, "¡Gracias por su acompañamiento diario y guía en los deberes escolares!"),
        BadgeOption("FAMILY_STEAM_PROJECT", "🧪", "Proyecto STEAM en Casa", "Construcción colaborativa de maquetas e inventos familiares.", 90, 180, "¡Gran trabajo en equipo familiar construyendo proyectos creativos y científicos!"),
        BadgeOption("FAMILY_VALUES", "💖", "Cuna de Valores", "Educación basada en la honestidad, respeto y sana convivencia.", 100, 200, "¡Homenaje a una familia que cultiva el respeto, la empatía y la honestidad!")
    )

    val currentPresets = if (selectedBadgeType == "FAMILY") familyPresetBadges else studentPresetBadges
    var selectedPreset by remember { mutableStateOf(familyPresetBadges.first()) }
    var teacherNote by remember { mutableStateOf(familyPresetBadges.first().defaultNote) }

    val effectiveTitle = if (creationMode == "CUSTOM") customTitle.trim() else selectedPreset.title
    val effectiveEmoji = if (creationMode == "CUSTOM") customEmoji.trim().ifBlank { "⭐" } else selectedPreset.emoji
    val effectiveDesc = if (creationMode == "CUSTOM") customDesc.trim().ifBlank { "Reconocimiento institucional por cumplimiento y mérito escolar." } else selectedPreset.desc
    val effectiveCredits = if (creationMode == "CUSTOM") customCredits else selectedPreset.credits
    val effectiveXp = if (creationMode == "CUSTOM") customXp else selectedPreset.xp
    val effectiveKey = if (creationMode == "CUSTOM") "CUSTOM_${System.currentTimeMillis()}" else selectedPreset.key

    val activePool = if (recipientRole == "PARENTS") parents else students
    val filteredPool = remember(activePool, recipientSearchQuery) {
        if (recipientSearchQuery.isBlank()) activePool else {
            activePool.filter {
                it.name.contains(recipientSearchQuery, ignoreCase = true) ||
                it.studentCode.contains(recipientSearchQuery, ignoreCase = true) ||
                it.email.contains(recipientSearchQuery, ignoreCase = true)
            }
        }
    }

    val themeColor = if (selectedBadgeType == "FAMILY") Color(0xFF7C3AED) else Color(0xFF2563EB)
    val themeContainerColor = if (selectedBadgeType == "FAMILY") Color(0xFFEDE9FE) else Color(0xFFEFF6FF)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeContainerColor,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(effectiveEmoji, fontSize = 22.sp)
                    }
                }
                Column {
                    Text(
                        text = "Condecoración Masiva & Rápida",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Crea o elige el logro, marca a quiénes dárselo y ¡pum, se manda!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Modo: Plantilla Rápida vs Crear Personalizado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val isPreset = creationMode == "PRESET"
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isPreset) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { creationMode = "PRESET" }
                        ) {
                            Text(
                                text = "📋 Plantillas Rápidas",
                                color = if (isPreset) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        val isCustom = creationMode == "CUSTOM"
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isCustom) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { creationMode = "CUSTOM" }
                        ) {
                            Text(
                                text = "✨ Crear Logro Nuevo",
                                color = if (isCustom) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                if (creationMode == "PRESET") {
                    // Selector Segmentado de Tipo de Medalla
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isFamily = selectedBadgeType == "FAMILY"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isFamily) Color(0xFF7C3AED) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedBadgeType = "FAMILY"
                                        recipientRole = "PARENTS"
                                        selectedPreset = familyPresetBadges.first()
                                        teacherNote = familyPresetBadges.first().defaultNote
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("👨‍👩‍👧", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Medalla Familiar",
                                        color = if (isFamily) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val isStudent = selectedBadgeType == "STUDENT"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isStudent) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedBadgeType = "STUDENT"
                                        recipientRole = "STUDENTS"
                                        selectedPreset = studentPresetBadges.first()
                                        teacherNote = studentPresetBadges.first().defaultNote
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎓", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mérito Alumno",
                                        color = if (isStudent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Catálogo de Medallas e Insignias - Carrusel de Chips y Tarjeta Destacada
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Selecciona la Medalla:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldStar.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "🪙 +${selectedPreset.credits} | ⚡ +${selectedPreset.xp} XP",
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Carrusel horizontal de insignias (Chips compactos)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(currentPresets) { preset ->
                                val isSelected = selectedPreset.key == preset.key
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedPreset = preset
                                        teacherNote = preset.defaultNote
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(preset.emoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = preset.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        // Tarjeta destacada de la medalla seleccionada actualmente
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeContainerColor,
                            border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(selectedPreset.emoji, fontSize = 22.sp)
                                        Text(
                                            text = selectedPreset.title,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.5.sp,
                                            color = themeColor
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SuccessGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "+${selectedPreset.credits} 🪙",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedPreset.desc,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Formulario de Creación de Logro Personalizado
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1. Datos del Nuevo Logro:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text("Título del Logro / Meta") },
                            placeholder = { Text("Ej: Asistencia a 1ª Reunión de Padres") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customEmoji,
                                onValueChange = { customEmoji = it },
                                label = { Text("Emoji") },
                                modifier = Modifier.weight(0.7f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = customCredits.toString(),
                                onValueChange = { customCredits = it.toIntOrNull() ?: 0 },
                                label = { Text("Créditos 🪙") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = customXp.toString(),
                                onValueChange = { customXp = it.toIntOrNull() ?: 0 },
                                label = { Text("Exp. XP ⚡") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = customDesc,
                            onValueChange = { customDesc = it },
                            label = { Text("Descripción o Motivo Institucional") },
                            placeholder = { Text("Ej: Reconocimiento por puntualidad y asistencia a la reunión...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2
                        )
                    }
                }

                // 2. Destinatarios Masivos con Casillas de Verificación
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. Destinatarios (${selectedUserIds.size} seleccionados):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Selector de Rol de Destinatario: Pestañas 50/50 balanceadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (recipientRole == "PARENTS") Color(0xFF7C3AED) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { recipientRole = "PARENTS" }
                        ) {
                            Text(
                                text = "👨‍👩‍👧 Padres (${parents.size})",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (recipientRole == "PARENTS") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (recipientRole == "STUDENTS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { recipientRole = "STUDENTS" }
                        ) {
                            Text(
                                text = "🎓 Alumnos (${students.size})",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (recipientRole == "STUDENTS") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Acciones rápidas de selección
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = recipientSearchQuery,
                            onValueChange = { recipientSearchQuery = it },
                            placeholder = { Text("Filtrar por nombre...", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        TextButton(
                            onClick = { selectedUserIds = activePool.map { it.id }.toSet() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Todos", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        TextButton(
                            onClick = { selectedUserIds = emptySet() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Ninguno", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }

                    // Lista de Personas con Checkbox
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val innerScrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .verticalScroll(innerScrollState)
                                .padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (filteredPool.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No se encontraron registros.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            } else {
                                filteredPool.forEach { user ->
                                    val isChecked = user.id in selectedUserIds
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedUserIds = if (isChecked) selectedUserIds - user.id else selectedUserIds + user.id
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedUserIds = if (checked) selectedUserIds + user.id else selectedUserIds - user.id
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = com.example.domain.validation.ValidationUtils.formatProperNoun(user.name),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (user.role == UserRole.PARENT.code) "Acudiente • ${user.email}" else "Estudiante • ${user.studentCode.ifBlank { "7° Grado" }}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
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

                // 3. Mensaje del Docente
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "3. Mensaje del Docente para el Boletín:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = teacherNote,
                        onValueChange = { teacherNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                // 4. Evidencia Fotográfica (Opcional)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📸 Foto o certificado (opcional):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (badgePhotoUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = badgePhotoUri,
                                contentDescription = "Foto del reconocimiento",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { badgePhotoUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = themeColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adjuntar Foto de Evidencia", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val category = if (selectedBadgeType == "FAMILY") "FAMILY" else "HONOR"
                    onGrantMultiple(
                        selectedUserIds.toList(),
                        effectiveKey,
                        effectiveTitle,
                        effectiveDesc,
                        effectiveEmoji,
                        category,
                        teacherNote.trim().ifBlank { effectiveTitle },
                        effectiveXp,
                        effectiveCredits,
                        badgePhotoUri
                    )
                    onDismiss()
                },
                enabled = selectedUserIds.isNotEmpty() && (creationMode != "CUSTOM" || customTitle.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_grant_badge_button")
            ) {
                Text(
                    text = "Otorgar a ${selectedUserIds.size} Persona${if (selectedUserIds.size == 1) "" else "s"} (+${effectiveCredits} 🪙) 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}

@Composable
fun AdminCourseSummarySection(
    totalStudents: Int,
    totalSubjects: Int,
    teachersCount: Int,
    eventsCount: Int,
    onDownloadPdf: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Director de Grupo & Grado
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E3A8A),
            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COLEGIO HOGAR MADRE DE DIOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD)
                        )
                        Text(
                            text = "Grado Séptimo (7°) • 2026-2027",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏫", fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👨‍🏫", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Director de Grupo:",
                                fontSize = 10.sp,
                                color = Color(0xFFBFDBFE),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ing. Manuel Alejandro Muñoz (Matemáticas & Informática)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Action Card to Generate and Download PDF
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📥", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Descarga de Resumen Oficial",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "Dossier completo en PDF oficial multi-página para padres, docentes y directivos",
                            fontSize = 11.sp,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
                        Text("Página 1: Horario Semanal 7° (07:00 a 13:20) + Ficha del Director", fontSize = 11.5.sp, color = Color(0xFF1E293B))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
                        Text("Páginas 2 y 3: Directorio Institucional de Docentes completo sin cortes", fontSize = 11.5.sp, color = Color(0xFF1E293B))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
                        Text("Páginas 4 y 5: Calendario Escolar Completo hasta Junio con Clausuras", fontSize = 11.5.sp, color = Color(0xFF1E293B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDownloadPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generar y Descargar Resumen (PDF)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Métricas del Curso - Cuadrícula 2x2 elegante y espaciosa
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data class CourseStatItem(val icon: String, val count: String, val label: String, val color: Color)
            val stats = listOf(
                CourseStatItem("🎓", "$totalStudents", "Alumnos Matriculados", Color(0xFF2563EB)),
                CourseStatItem("📚", "$totalSubjects", "Materias Oficiales", Color(0xFF059669)),
                CourseStatItem("👨‍🏫", "$teachersCount", "Profesores de 7°", Color(0xFF7C3AED)),
                CourseStatItem("🗓️", "$eventsCount", "Eventos del Calendario", Color(0xFFD97706))
            )
            stats.chunked(2).forEach { rowStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowStats.forEach { stat ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = stat.color.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, stat.color.copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = stat.color.copy(alpha = 0.16f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = stat.icon, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stat.count,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = stat.color
                                    )
                                    Text(
                                        text = stat.label,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradeExamDialog(
    exam: ExamEntity,
    onDismiss: () -> Unit,
    onSaveGrade: (grade: Double, feedback: String) -> Unit
) {
    var gradeScore by remember { mutableDoubleStateOf(exam.grade ?: 4.5) }
    var feedback by remember { mutableStateOf(exam.teacherFeedback ?: "¡Buen desarrollo!") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar Evaluación", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Evaluación: ${exam.title} (${exam.subject})", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nota Oficial:")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (gradeScore >= 3.0) SuccessGreen else DangerRed
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", gradeScore)} / 5.0",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Slider(
                    value = gradeScore.toFloat(),
                    onValueChange = { gradeScore = ((it * 10).toInt() / 10.0).coerceIn(1.0, 5.0) },
                    valueRange = 1.0f..5.0f,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth().testTag("exam_grade_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Retroalimentación del docente") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveGrade(gradeScore, feedback)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_grade_button")
            ) {
                Text("Guardar Calificación")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AdminSubjectCard(
    subject: SubjectEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(subject.colorHex).copy(alpha = 0.35f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(subject.colorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(subject.emoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subject.teacherName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subject.description.isNotBlank()) {
                        Text(
                            text = subject.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar materia",
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, emoji: String, teacherName: String, classroom: String, colorHex: Long, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("Prof. Titular") }
    var classroom by remember { mutableStateOf("Aula 101") }
    var description by remember { mutableStateOf("") }

    val presetEmojis = listOf("📚", "📐", "🔬", "💻", "🎨", "⚽", "🌍", "📖", "⚡", "🧪", "🎭", "🎵", "🤖", "🏛️", "🧭")
    var selectedEmoji by remember { mutableStateOf("📚") }

    val presetColors = listOf(
        0xFF2563EB to "Azul",
        0xFFE11D74 to "Magenta",
        0xFF10B981 to "Verde",
        0xFFF59E0B to "Ámbar",
        0xFF8B5CF6 to "Púrpura",
        0xFFEF4444 to "Rojo",
        0xFF06B6D4 to "Cian",
        0xFFEC4899 to "Rosa"
    )
    var selectedColor by remember { mutableStateOf(presetColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Asignatura", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la materia") },
                    placeholder = { Text("Ej: Robótica") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Icono / Emoji:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetEmojis.forEach { em ->
                        val isSel = selectedEmoji == em
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedEmoji = em }
                        ) {
                            Text(em, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Text("Color Temático:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetColors.forEach { (colorVal, colorLabel) ->
                        val isSel = selectedColor.first == colorVal
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(colorVal) else Color(colorVal).copy(alpha = 0.2f),
                            modifier = Modifier.clickable { selectedColor = colorVal to colorLabel }
                        ) {
                            Text(
                                text = colorLabel,
                                color = if (isSel) Color.White else Color(colorVal),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text("Docente Asignado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Nombre del Docente") },
                    placeholder = { Text("Selecciona de la lista o escribe...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Docentes del Directorio Institucional:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OFFICIAL_TEACHER_DIRECTORY.filter { it.roleOrGrade.contains("Director", ignoreCase = true) || it.roleOrGrade.contains("Docente", ignoreCase = true) || it.roleOrGrade.contains("Psic", ignoreCase = true) || it.roleOrGrade.contains("Prof", ignoreCase = true) }.forEach { teacher ->
                        val isSel = teacherName == teacher.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { teacherName = teacher.name }
                        ) {
                            Text(
                                text = "${teacher.avatarEmoji} ${teacher.name.split(" ").take(2).joinToString(" ")}",
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Detalles o temas principales...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, selectedEmoji, teacherName, "", selectedColor.first, description)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Crear Asignatura")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AdminUserRowCard(
    user: UserEntity,
    isCurrentUser: Boolean,
    onPhotoClick: () -> Unit,
    onEditClick: () -> Unit,
    onAdjustPointsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val roleBadgeColor = when (user.role) {
        UserRole.TEACHER.code -> GoldStar
        UserRole.PARENT.code -> StreakOrange
        else -> MaterialTheme.colorScheme.primary
    }

    val roleLabel = when (user.role) {
        UserRole.TEACHER.code -> "Docente"
        UserRole.PARENT.code -> "Acudiente"
        else -> "Estudiante"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isCurrentUser) GoldStar.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = if (isCurrentUser) 3.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User Avatar (clickable to view large profile photo)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(user.avatarColorHex).copy(alpha = 0.2f))
                        .clickable { onPhotoClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = user.photoUri,
                            contentDescription = "Foto de ${user.name}",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = user.avatarEmoji.ifBlank { "👤" },
                            fontSize = 24.sp
                        )
                    }
                }

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = com.example.domain.validation.ValidationUtils.formatProperNoun(user.name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldStar.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Tú 👑",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldStar,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = roleBadgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = roleLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = roleBadgeColor,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (user.gradeSection.isNotBlank()) {
                            Text(
                                text = user.gradeSection,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-bar with Points info & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Points & XP Pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "🪙 ${user.credits} pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldStar.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "⚡ ${user.xp} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (user.studentCode.isNotBlank()) {
                        Text(
                            text = "ID: ${user.studentCode}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Actions (Edit, Points, Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onAdjustPointsClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Ajustar Puntos",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Usuario",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!isCurrentUser) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Usuario",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, email: String, role: String, gradeSection: String, credits: Int, xp: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT.code) }
    var gradeSection by remember { mutableStateOf("10° Grado") }
    var creditsStr by remember { mutableStateOf("100") }
    var xpStr by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Usuario", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Rol Institucional:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        UserRole.STUDENT.code to "🎓 Alumno",
                        UserRole.TEACHER.code to "👨‍🏫 Docente",
                        UserRole.PARENT.code to "👨‍👩‍👧 Padre"
                    ).forEach { (rCode, rLabel) ->
                        val isSelected = selectedRole == rCode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedRole = rCode }
                        ) {
                            Text(
                                text = rLabel,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = gradeSection,
                    onValueChange = { gradeSection = it },
                    label = { Text(if (selectedRole == UserRole.TEACHER.code) "Materia / Asignatura" else "Grado / Sección") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditsStr,
                        onValueChange = { creditsStr = it },
                        label = { Text("Créditos 🪙") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = xpStr,
                        onValueChange = { xpStr = it },
                        label = { Text("XP Inicial ⚡") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onAdd(
                            name,
                            email,
                            selectedRole,
                            gradeSection,
                            creditsStr.toIntOrNull() ?: 100,
                            xpStr.toIntOrNull() ?: 50
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Guardar Persona")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AdminEditUserDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, role: String, gradeSection: String, credits: Int, xp: Int, code: String) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var role by remember { mutableStateOf(user.role) }
    var gradeSection by remember { mutableStateOf(user.gradeSection) }
    var creditsStr by remember { mutableStateOf(user.credits.toString()) }
    var xpStr by remember { mutableStateOf(user.xp.toString()) }
    var code by remember { mutableStateOf(if (user.role == UserRole.TEACHER.code) user.teacherCode else user.studentCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = gradeSection,
                    onValueChange = { gradeSection = it },
                    label = { Text("Grado / Materia") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditsStr,
                        onValueChange = { creditsStr = it },
                        label = { Text("Créditos 🪙") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = xpStr,
                        onValueChange = { xpStr = it },
                        label = { Text("Puntos XP ⚡") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Código Único") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        email,
                        role,
                        gradeSection,
                        creditsStr.toIntOrNull() ?: user.credits,
                        xpStr.toIntOrNull() ?: user.xp,
                        code
                    )
                    onDismiss()
                }
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
fun AdminAdjustPointsDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onAdjust: (deltaCredits: Int, deltaXp: Int, reason: String) -> Unit
) {
    var isAdd by remember { mutableStateOf(true) }
    var amountStr by remember { mutableStateOf("50") }
    var reason by remember { mutableStateOf("Reconocimiento académico") }

    val presetValues = if (isAdd) listOf("25", "50", "100", "200") else listOf("20", "50", "100")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Escolaris a ${user.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Saldo actual: 🪙 ${user.credits} Escolaris • ⚡ ${user.xp} XP",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isAdd = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAdd) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("➕ Otorgar", color = if (isAdd) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isAdd = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isAdd) DangerRed else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("➖ Deducir", color = if (!isAdd) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }

                // Quick Presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    presetValues.forEach { pVal ->
                        val isSelected = amountStr == pVal
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) (if (isAdd) SuccessGreen else DangerRed) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountStr = pVal }
                        ) {
                            Text(
                                text = if (isAdd) "+$pVal 🪙" else "-$pVal 🪙",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountStr = it },
                    label = { Text("Cantidad de Escolaris 🪙") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo") },
                    placeholder = { Text("Ej: Participación destacada, Proyecto...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toIntOrNull() ?: 50
                    val deltaCredits = if (isAdd) amount else -amount
                    val deltaXp = if (isAdd) amount * 2 else 0
                    onAdjust(deltaCredits, deltaXp, reason)
                    onDismiss()
                }
            ) {
                Text(if (isAdd) "Otorgar 🪙" else "Deducir 🪙")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminGroupAdjustPointsDialog(
    allUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (targetUserIds: List<String>, deltaCredits: Int, deltaXp: Int, reason: String) -> Unit
) {
    var isAdd by remember { mutableStateOf(true) }
    var amountStr by remember { mutableStateOf("50") }
    var reason by remember { mutableStateOf("Reconocimiento grupal / Asistencia") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserIds by remember { mutableStateOf(setOf<String>()) }

    val presetValues = if (isAdd) listOf("25", "50", "100", "200") else listOf("20", "50", "100")

    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) allUsers else {
            val q = searchQuery.trim().lowercase()
            allUsers.filter {
                it.name.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.role.lowercase().contains(q) ||
                it.gradeSection.lowercase().contains(q)
            }
        }
    }

    val studentCount = remember(allUsers) { allUsers.count { it.role == UserRole.STUDENT.code } }
    val parentCount = remember(allUsers) { allUsers.count { it.role == UserRole.PARENT.code } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🪙", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajuste de Puntos en Grupo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selector Otorgar vs Deducir
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isAdd = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAdd) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("➕ Otorgar", color = if (isAdd) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { isAdd = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isAdd) DangerRed else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("➖ Deducir", color = if (!isAdd) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    presetValues.forEach { pVal ->
                        val isSelected = amountStr == pVal
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) (if (isAdd) SuccessGreen else DangerRed) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountStr = pVal }
                        ) {
                            Text(
                                text = if (isAdd) "+$pVal 🪙" else "-$pVal 🪙",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountStr = it },
                    label = { Text("Cantidad por usuario (🪙)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo grupal") },
                    placeholder = { Text("Ej: Asistencia a reunión, proyecto...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Botones rápidos de selección
                Text("Seleccionar usuarios:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.clickable {
                            selectedUserIds = allUsers.filter { it.role == UserRole.STUDENT.code }.map { it.id }.toSet()
                        }
                    ) {
                        Text("Alumnos ($studentCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.clickable {
                            selectedUserIds = allUsers.filter { it.role == UserRole.PARENT.code }.map { it.id }.toSet()
                        }
                    ) {
                        Text("Padres ($parentCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                        modifier = Modifier.clickable {
                            selectedUserIds = allUsers.map { it.id }.toSet()
                        }
                    ) {
                        Text("Todos (${allUsers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            selectedUserIds = emptySet()
                        }
                    ) {
                        Text("Ninguno", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filtrar por nombre o grado...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${selectedUserIds.size} de ${allUsers.size} seleccionados",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        filteredUsers.forEach { user ->
                            val isChecked = selectedUserIds.contains(user.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedUserIds = if (isChecked) {
                                            selectedUserIds - user.id
                                        } else {
                                            selectedUserIds + user.id
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedUserIds = if (checked) {
                                            selectedUserIds + user.id
                                        } else {
                                            selectedUserIds - user.id
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = com.example.domain.validation.ValidationUtils.formatProperNoun(user.name),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${user.role} • 🪙 ${user.credits}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toIntOrNull() ?: 50
                    val deltaCredits = if (isAdd) amount else -amount
                    val deltaXp = if (isAdd) amount * 2 else 0
                    onConfirm(selectedUserIds.toList(), deltaCredits, deltaXp, reason)
                },
                enabled = selectedUserIds.isNotEmpty() && (amountStr.toIntOrNull() ?: 0) > 0,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isAdd) "Otorgar a ${selectedUserIds.size} 🪙" else "Deducir a ${selectedUserIds.size} 🪙",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AdminObligationAttendanceDialog(
    obligation: ParentObligationEntity,
    allUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (selectedUserIds: List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentUsers = remember(allUsers) {
        allUsers.filter { it.role == UserRole.PARENT.code }
    }
    val candidateUsers = remember(parentUsers, allUsers) {
        if (parentUsers.isNotEmpty()) parentUsers else allUsers
    }
    var selectedUserIds by remember {
        mutableStateOf(candidateUsers.map { it.id }.toSet())
    }

    val filteredUsers = remember(candidateUsers, searchQuery) {
        if (searchQuery.isBlank()) candidateUsers else {
            val q = searchQuery.trim().lowercase()
            candidateUsers.filter {
                it.name.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.studentCode.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎖️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asistencia: ${obligation.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                            Text(obligation.rewardBadgeEmoji.ifBlank { "🎖️" }, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Insignia: ${obligation.rewardBadgeTitle}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                                Text("+${obligation.rewardCredits} 🪙 Escolaris y ${obligation.rewardXp} XP", fontSize = 10.sp, color = Color(0xFFB45309))
                            }
                        }
                    }
                }

                Text(
                    text = "Selecciona las familias que asistieron o cumplieron:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.clickable {
                            selectedUserIds = candidateUsers.map { it.id }.toSet()
                        }
                    ) {
                        Text(
                            text = "Todos (${candidateUsers.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            selectedUserIds = emptySet()
                        }
                    ) {
                        Text(
                            text = "Ninguno",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filtrar por nombre...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${selectedUserIds.size} de ${candidateUsers.size} seleccionados",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        filteredUsers.forEach { user ->
                            val isChecked = selectedUserIds.contains(user.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedUserIds = if (isChecked) {
                                            selectedUserIds - user.id
                                        } else {
                                            selectedUserIds + user.id
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedUserIds = if (checked) {
                                            selectedUserIds + user.id
                                        } else {
                                            selectedUserIds - user.id
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = com.example.domain.validation.ValidationUtils.formatProperNoun(user.name),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${user.role} • 🪙 ${user.credits} Escolaris",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedUserIds.toList())
                },
                enabled = selectedUserIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Otorgar a ${selectedUserIds.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AdminTeacherDirectoryCard(
    teacher: TeacherDirectoryEntry,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.25f)),
        shadowElevation = 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEDE9FE),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(teacher.avatarEmoji, fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = teacher.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = teacher.roleOrGrade,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7C3AED),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (teacher.subject.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📚", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = teacher.subject,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📅", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = teacher.attentionDay,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDBEAFE),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = teacher.attentionHours,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E40AF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (!teacher.email.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✉️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = teacher.email,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPenaltyCard(
    penalty: com.example.data.local.entity.PenaltyEntity,
    onRevoke: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val isRevoked = penalty.status == "REVOCADA"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isRevoked) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else DangerRed.copy(alpha = 0.35f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = penalty.studentName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DangerRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "-${penalty.pointsDeducted} 🪙 pts",
                                color = DangerRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Falta: ${penalty.reason}",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (penalty.observation.isNotBlank()) {
                        Text(
                            text = "Obs: \"${penalty.observation}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Docente: ${penalty.teacherName} • ${dateFormat.format(Date(penalty.timestamp))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isRevoked) {
                    OutlinedButton(
                        onClick = onRevoke,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Anular / Devolver", fontSize = 10.sp)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text("Anulada", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IssuePenaltyDialog(
    students: List<UserEntity>,
    onDismiss: () -> Unit,
    onIssue: (studentId: String, studentName: String, reason: String, category: String, pointsDeducted: Int, observation: String, notifyParents: Boolean) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }

    val infractionPresets = listOf(
        Triple("Uso Indebido de Celular en Clase", "CELLPHONE", 30),
        Triple("Indisciplina / Falta de Respeto", "DISCIPLINE", 50),
        Triple("No Portar Uniforme Institucional", "UNIFORM", 15),
        Triple("Incumplimiento de Tarea / Deberes", "HOMEWORK", 20),
        Triple("Daño o Mal Uso de Mobiliario / Aulas", "FACILITY", 100),
        Triple("Retardo Reincidente a Clase", "LATE", 10),
        Triple("Falta Personalizada", "OTHER", 25)
    )

    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var customReason by remember { mutableStateOf("") }
    var customPointsStr by remember { mutableStateOf("25") }
    var observation by remember { mutableStateOf("") }
    var notifyParents by remember { mutableStateOf(true) }

    val currentPreset = infractionPresets[selectedPresetIndex]
    val isCustom = selectedPresetIndex == infractionPresets.lastIndex
    val effectivePoints = if (isCustom) (customPointsStr.toIntOrNull() ?: 25) else currentPreset.third

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DangerRed.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🚨", fontSize = 20.sp)
                    }
                }
                Column {
                    Text("Aplicar Multa Disciplinaria", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DangerRed)
                    Text("Descuento de créditos y notificación a padres", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                // 1. Selector Desplegable de Estudiantes
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("1. Selecciona el Estudiante a Sancionar:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    StudentDropdownField(
                        students = students,
                        selectedStudent = selectedStudent,
                        onStudentSelected = { selectedStudent = it },
                        label = "Estudiante"
                    )
                }

                // 2. Tipo de Falta
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Tipo de Falta / Infracción:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DangerRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "-$effectivePoints 🪙 Escolaris",
                                color = DangerRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        infractionPresets.forEachIndexed { index, (presetName, _, defaultPts) ->
                            val isSelected = selectedPresetIndex == index
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) DangerRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) DangerRed else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPresetIndex = index }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = presetName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) DangerRed else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) DangerRed else DangerRed.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "-$defaultPts 🪙",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else DangerRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Falta personalizada (solo si elige la última opción)
                if (isCustom) {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Describe la falta personalizada") },
                        placeholder = { Text("Ej: Interrupción continua de clase...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = customPointsStr,
                        onValueChange = { customPointsStr = it },
                        label = { Text("Créditos a Descontar (🪙 Escolaris)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 4. Observación del Docente
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("3. Observación para los Padres:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = observation,
                        onValueChange = { observation = it },
                        placeholder = { Text("Ej: Diálogo realizado con el estudiante...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                // 5. Notificación inmediata
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = notifyParents, onCheckedChange = { notifyParents = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Notificar a los Padres Inmediatamente 📢", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Envía notificación push al acudiente registrado", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = selectedStudent ?: students.firstOrNull()
                    val targetId = target?.id ?: "student_${System.currentTimeMillis()}"
                    val targetName = target?.name ?: "Estudiante"
                    val finalReason = if (isCustom && customReason.isNotBlank()) customReason else currentPreset.first

                    onIssue(targetId, targetName, finalReason, currentPreset.second, effectivePoints, observation, notifyParents)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Aplicar Sanción (-$effectivePoints 🪙)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AdminParentObligationCard(
    obligation: ParentObligationEntity,
    onBroadcastReminder: () -> Unit,
    onAwardAttendance: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    val isCompleted = obligation.isCompleted
    val isPension = obligation.category == "PENSION"
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

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
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.2.dp, if (isPension) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPension) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "⏰ 2:00 PM (Días 1-5)",
                                color = Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Column {
                Text(
                    text = obligation.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = obligation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reward details
            // Reward details (Single line, strictly using +X 🪙)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
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
                            text = "Insignia: ${obligation.rewardBadgeTitle}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${obligation.rewardCredits} 🪙",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309),
                        maxLines = 1
                    )
                }
            }

            // System Push Notification Preview & Automatic Schedule Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔔", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Recordatorio para Familias:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = "Automático",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Text(
                        text = "\"${obligation.whatsappMessage}\"",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    if ((obligation.lastReminderSentMillis ?: 0L) > 0L) {
                        Text(
                            text = "Último recordatorio enviado: ${dateFormat.format(Date(obligation.lastReminderSentMillis!!))}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBroadcastReminder,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.NotificationImportant, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🔔 Notificar", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }

                if (onAwardAttendance != null) {
                    Button(
                        onClick = onAwardAttendance,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🎖️ Validar y Dar Puntos", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun AddParentObligationDialog(
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        description: String,
        category: String,
        month: String,
        dueDay: Int,
        badgeKey: String,
        badgeTitle: String,
        badgeEmoji: String,
        credits: Int,
        xp: Int,
        whatsappMessage: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("Pago Oportuno de Pensión Escolar") }
    var description by remember { mutableStateOf("Pago de la pensión escolar correspondiente al mes de Octubre durante los primeros 5 días.") }
    var category by remember { mutableStateOf("PENSION") }
    var month by remember { mutableStateOf("Octubre") }
    var dueDayStr by remember { mutableStateOf("5") }
    var badgeKey by remember { mutableStateOf("PARENT_PENSION_OCTUBRE") }
    var badgeTitle by remember { mutableStateOf("Pago Oportuno de Pensión (Octubre)") }
    var badgeEmoji by remember { mutableStateOf("💳") }
    var creditsStr by remember { mutableStateOf("100") }
    var xpStr by remember { mutableStateOf("150") }
    var whatsappMessage by remember {
        mutableStateOf("¡Hola estimado acudiente! 👋 Les recordamos que a partir de octubre la pensión se paga los 5 primeros días del mes. ¡Paga a tiempo para ganar la insignia de Pago Oportuno y +100 créditos Escolaris para tu hijo/a! ⭐")
    }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val monthNameFormatter = remember { SimpleDateFormat("MMMM", Locale("es", "ES")) }

    var publishDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val initialDueCal = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) } }
    var dueDateMillis by remember { mutableStateOf(initialDueCal.timeInMillis) }

    var publishDateText by remember { mutableStateOf(dateFormatter.format(publishDateMillis)) }
    var dueDateText by remember { mutableStateOf(dateFormatter.format(dueDateMillis)) }

    val presets = listOf(
        listOf("Pensión Mensual", "Pensión", "💳", "PENSION"),
        listOf("Firma de Circular", "Circular", "📄", "DOCUMENTATION"),
        listOf("Asamblea General", "Asamblea", "🏛️", "EVENT")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDCF8C6),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📋", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Nuevo Deber Familiar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Asigna deberes y recordatorios estilo WhatsApp", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
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
                Text("Plantillas rápidas:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val presetTitle = preset[0]
                        val presetSpanishName = preset[1]
                        val presetEmoji = preset[2]
                        val presetCat = preset[3]
                        val isSelected = category == presetCat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF25D366).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF25D366) else Color.Transparent),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    category = presetCat
                                    badgeEmoji = presetEmoji
                                    when (presetCat) {
                                        "PENSION" -> {
                                            title = "Pago Oportuno de Pensión Escolar"
                                            description = "Pago de la pensión escolar correspondiente al mes de $month durante los primeros 5 días."
                                            badgeKey = "PARENT_PENSION_${month.uppercase()}"
                                            badgeTitle = "Pago Oportuno de Pensión ($month)"
                                            creditsStr = "100"
                                            xpStr = "150"
                                            whatsappMessage = "¡Hola estimado acudiente! 👋 Les recordamos que a partir de $month la pensión se paga los 5 primeros días del mes. ¡Paga a tiempo para ganar la insignia de Pago Oportuno y +100 créditos Escolaris para tu hijo/a! ⭐"
                                        }
                                        "DOCUMENTATION" -> {
                                            title = "Firma de Circular Institucional"
                                            description = "Revisión y firma digital de la circular informativa institucional del mes."
                                            badgeKey = "PARENT_CIRCULAR_${month.uppercase()}"
                                            badgeTitle = "Acudiente Informado ($month)"
                                            creditsStr = "60"
                                            xpStr = "100"
                                            whatsappMessage = "¡Hola estimado acudiente! 👋 Recuerde firmar la circular escolar institucional antes del 10 de $month. ¡Suma +60 créditos para tu hijo/a!"
                                        }
                                        "EVENT" -> {
                                            title = "Asamblea General de Padres"
                                            description = "Asistencia presencial o virtual a la reunión institucional de acudientes."
                                            badgeKey = "PARENT_ASSEMBLY_${month.uppercase()}"
                                            badgeTitle = "Compromiso Familiar ($month)"
                                            creditsStr = "80"
                                            xpStr = "120"
                                            whatsappMessage = "¡Hola familia Escolaris! 🏛️ Los esperamos con entusiasmo en la Asamblea General este mes. ¡Tu asistencia condecora a tu hijo/a con +80 créditos!"
                                        }
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(presetEmoji, fontSize = 18.sp)
                                Text(presetSpanishName, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la Obligación") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = publishDateText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha Publicación") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Fecha publicación",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    val c = Calendar.getInstance().apply { timeInMillis = publishDateMillis }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val sel = Calendar.getInstance().apply { set(y, m, d) }
                                            publishDateMillis = sel.timeInMillis
                                            publishDateText = dateFormatter.format(sel.timeInMillis)
                                        },
                                        c.get(Calendar.YEAR),
                                        c.get(Calendar.MONTH),
                                        c.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = dueDateText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha Límite") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Fecha límite",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    val c = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val sel = Calendar.getInstance().apply { set(y, m, d) }
                                            dueDateMillis = sel.timeInMillis
                                            dueDateText = dateFormatter.format(sel.timeInMillis)
                                            val spanishMonth = monthNameFormatter.format(sel.timeInMillis).replaceFirstChar { it.uppercase() }
                                            month = spanishMonth
                                            dueDayStr = d.toString()
                                            if (category == "PENSION") {
                                                badgeTitle = "Pago Oportuno de Pensión ($spanishMonth)"
                                                badgeKey = "PARENT_PENSION_${spanishMonth.uppercase()}"
                                            } else if (category == "DOCUMENTATION") {
                                                badgeTitle = "Acudiente Informado ($spanishMonth)"
                                                badgeKey = "PARENT_CIRCULAR_${spanishMonth.uppercase()}"
                                            } else if (category == "EVENT") {
                                                badgeTitle = "Compromiso Familiar ($spanishMonth)"
                                                badgeKey = "PARENT_ASSEMBLY_${spanishMonth.uppercase()}"
                                            }
                                        },
                                        c.get(Calendar.YEAR),
                                        c.get(Calendar.MONTH),
                                        c.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = badgeTitle,
                        onValueChange = { badgeTitle = it },
                        label = { Text("Nombre de la Insignia") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = badgeEmoji,
                        onValueChange = { badgeEmoji = it },
                        label = { Text("Emoji") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = creditsStr,
                        onValueChange = { creditsStr = it },
                        label = { Text("Créditos para el Hijo/a") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = xpStr,
                        onValueChange = { xpStr = it },
                        label = { Text("XP para el Hijo/a") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = whatsappMessage,
                    onValueChange = { whatsappMessage = it },
                    label = { Text("Mensaje WhatsApp Programado (2:00 PM)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dueDay = dueDayStr.toIntOrNull() ?: 5
                    val creds = creditsStr.toIntOrNull() ?: 100
                    val xpVal = xpStr.toIntOrNull() ?: 150
                    onAdd(title, description, category, month, dueDay, badgeKey, badgeTitle, badgeEmoji, creds, xpVal, whatsappMessage)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Guardar y Programar Recordatorio",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


