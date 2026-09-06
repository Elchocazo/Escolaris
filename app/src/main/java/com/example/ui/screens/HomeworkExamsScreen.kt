package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.SchoolEventEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.TaskPriority
import com.example.domain.model.TaskStatus
import com.example.domain.model.UserRole
import com.example.ui.components.ExamScannerDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldStar
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel
import com.example.utils.CalendarSyncHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeworkExamsScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allSchoolEvents by viewModel.allSchoolEvents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val isTeacher = currentUser?.role == UserRole.TEACHER.code
    val isParent = currentUser?.role == UserRole.PARENT.code
    val isParentLinked = isParent && !currentUser?.linkedStudentId.isNullOrBlank()

    val visibleTasks = remember(tasks, isTeacher, isParent, isParentLinked, currentUser) {
        when {
            isTeacher -> tasks
            isParent -> if (isParentLinked) tasks.filter { it.studentId == currentUser?.linkedStudentId } else emptyList()
            else -> tasks.filter { it.studentId == currentUser?.id }
        }
    }

    val visibleExams = remember(exams, isTeacher, isParent, isParentLinked, currentUser) {
        when {
            isTeacher -> exams
            isParent -> if (isParentLinked) exams.filter { it.studentId == currentUser?.linkedStudentId } else emptyList()
            else -> exams.filter { it.studentId == currentUser?.id }
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var showAddEventDateMillis by remember { mutableStateOf<Long?>(null) }

    val tabTitles = if (isParent) {
        listOf("📚 Tareas", "📝 Evaluaciones")
    } else if (isTeacher) {
        listOf("📅 Calendario", "📚 Tareas", "📝 Evaluaciones")
    } else {
        listOf("📅 Calendario", "📚 Tareas", "📝 Evaluaciones")
    }

    val isCalendarTab = !isParent && selectedTabIndex == 0
    val isTasksTab = (isParent && selectedTabIndex == 0) || (!isParent && selectedTabIndex == 1)
    val isExamsTab = (isParent && selectedTabIndex == 1) || (!isParent && selectedTabIndex == 2)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            val parts = title.split(" ", limit = 2)
                            val emoji = parts.getOrNull(0) ?: ""
                            val label = parts.getOrNull(1) ?: title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = emoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    )
                }
            }

            if (isCalendarTab) {
                // CALENDAR TAB (Docentes y Estudiantes)
                MonthlyCalendarView(
                    tasks = visibleTasks,
                    exams = visibleExams,
                    schedules = schedules,
                    events = allSchoolEvents,
                    onAddTask = { if (!isParent) showAddTaskDialog = true },
                    onAddEvent = if (isTeacher) { { dateMillis -> showAddEventDateMillis = dateMillis } } else null,
                    onDeleteEvent = if (isTeacher) { { viewModel.deleteSchoolEvent(it) } } else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = when {
                                        isTasksTab && isTeacher -> "Tareas Asignadas"
                                        isTasksTab && isParent -> "Tareas del Estudiante"
                                        isTasksTab -> "Mis Tareas"
                                        isTeacher -> "Evaluaciones y Calificaciones"
                                        isParent -> "Evaluaciones y Exámenes del Estudiante"
                                        else -> "Evaluaciones y Exámenes"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when {
                                        isTasksTab && isTeacher -> "Publica y programa las tareas escolares para tus alumnos"
                                        isTasksTab && isParent -> "Revisa las entregas y tareas pendientes de tu acudido"
                                        isTasksTab -> "Gestiona tus tareas escolares y fechas de entrega"
                                        isTeacher -> "Programa exámenes y califica las evaluaciones de la clase"
                                        isParent -> "Consulta las notas, fechas de examen y comentarios docentes"
                                        else -> "Registra calificaciones y agenda fechas de evaluaciones"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!isParent) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (isTasksTab) showAddTaskDialog = true
                                            else showAddExamDialog = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("add_item_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when {
                                                isTasksTab && isTeacher -> "Asignar Tarea"
                                                isTasksTab -> "Nueva Tarea"
                                                isTeacher -> "Agendar Evaluación"
                                                else -> "Agendar Examen"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isTasksTab) {
                        // TASKS TAB
                        if (isParent && !isParentLinked) {
                            item {
                                EmptyStateCard(
                                    message = "🔒 Para ver las tareas de tu hijo/a, vincula su código en la pestaña 'Padres'."
                                )
                            }
                        } else if (visibleTasks.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = if (isTeacher) "No hay tareas asignadas para la clase." else if (isParent) "Tu hijo/a no tiene tareas pendientes registradas." else "No tienes tareas pendientes. ¡Buen trabajo!"
                                )
                            }
                        } else {
                            items(visibleTasks, key = { it.id }) { task ->
                                TaskItemCard(
                                    task = task,
                                    isTeacher = isTeacher,
                                    isParent = isParent,
                                    onToggleStatus = { if (!isParent) viewModel.toggleTaskStatus(task) },
                                    onDelete = { if (!isParent) viewModel.deleteTask(task) },
                                    onSyncToCalendar = { CalendarSyncHelper.syncTaskToDeviceCalendar(context, task) }
                                )
                            }
                        }
                    } else if (isExamsTab) {
                        // EXAMS TAB
                        if (isParent && !isParentLinked) {
                            item {
                                EmptyStateCard(
                                    message = "🔒 Para ver las evaluaciones y notas de tu hijo/a, vincula su código en la pestaña 'Padres'."
                                )
                            }
                        } else if (visibleExams.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = if (isTeacher) "No hay evaluaciones registradas ni agendadas para la clase." else if (isParent) "No hay exámenes registrados para tu hijo/a." else "No hay exámenes registrados ni agendados."
                                )
                            }
                        } else {
                            items(visibleExams, key = { it.id }) { exam ->
                                ExamItemCard(
                                    exam = exam,
                                    onSyncToCalendar = { CalendarSyncHelper.syncExamToDeviceCalendar(context, exam) }
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
    }

    if (showAddEventDateMillis != null) {
        AddCalendarEventDialog(
            initialDateMillis = showAddEventDateMillis!!,
            onDismiss = { showAddEventDateMillis = null },
            onAdd = { title, category, dateMillis, time, location, description ->
                viewModel.addSchoolEvent(title, category, dateMillis, time, location, description)
                showAddEventDateMillis = null
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            availableSubjects = allSubjects,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, subject, desc, dueMillis, priority ->
                viewModel.addNewTask(title, subject, desc, dueMillis, priority)
            }
        )
    }

    if (showAddExamDialog) {
        AddExamDialog(
            availableSubjects = allSubjects,
            onDismiss = { showAddExamDialog = false },
            onAdd = { title, subject, dateMillis, classroom, topics ->
                viewModel.addNewExamSchedule(title, subject, dateMillis, classroom, topics)
            }
        )
    }

    if (showScannerDialog) {
        ExamScannerDialog(
            onDismiss = { showScannerDialog = false },
            onSaveExam = { title, subject, dateMillis, grade, classroom, topics, teacherFeedback, photoUri ->
                viewModel.registerScannedExam(
                    title = title,
                    subject = subject,
                    dateMillis = dateMillis,
                    grade = grade,
                    classroom = classroom,
                    topics = topics,
                    teacherFeedback = teacherFeedback,
                    scannedPhotoUri = photoUri
                )
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    isTeacher: Boolean = false,
    isParent: Boolean = false,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onSyncToCalendar: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED.code
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

    val priorityColor = when (task.priority) {
        TaskPriority.ALTA.code -> DangerRed
        TaskPriority.MEDIA.code -> GoldStar
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted && !isTeacher) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = if (isCompleted && !isTeacher) 0.dp else 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("task_card_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isTeacher || isParent) {
                Surface(
                    shape = CircleShape,
                    color = if (isCompleted) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (isCompleted) "✅" else "📚", fontSize = 16.sp)
                    }
                }
            } else {
                IconButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.size(32.dp).testTag("task_toggle_${task.id}")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isCompleted) "Completada" else "Pendiente",
                        tint = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isCompleted && !isTeacher && !isParent) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isCompleted && !isTeacher && !isParent) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = task.priority,
                            color = priorityColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${task.subject} • Vence: ${dateFormat.format(Date(task.dueDateMillis))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldStar.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isTeacher) "🪙 +${task.rewardCredits} pts para alumnos" else if (isParent) "🪙 +${task.rewardCredits} créditos asignados" else "🪙 +${task.rewardCredits} créditos al entregar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row {
                        IconButton(onClick = onSyncToCalendar, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Calendario", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        if (!isParent) {
                            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamItemCard(
    exam: ExamEntity,
    onSyncToCalendar: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM - HH:mm", Locale.getDefault())
    val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("exam_card_${exam.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${exam.subject} • ${exam.classroom}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (exam.isGraded && exam.grade != null) {
                    val isApproved = exam.grade >= 3.0
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isApproved) SuccessGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isApproved) SuccessGreen else DangerRed)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", exam.grade)} / 5.0",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isApproved) SuccessGreen else DangerRed,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Programado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📅 Fecha: ${dateOnlyFormat.format(Date(exam.examDateMillis))}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (exam.topics.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📖 Temario: ${exam.topics}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!exam.teacherFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💬 Docente: \"${exam.teacherFeedback}\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (exam.rewardCreditsEarned > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldStar.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "🪙 Ganaste +${exam.rewardCreditsEarned} créditos",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                TextButton(onClick = onSyncToCalendar) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recordatorio Calendario", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    availableSubjects: List<SubjectEntity> = emptyList(),
    onDismiss: () -> Unit,
    onAdd: (title: String, subject: String, desc: String, dueMillis: Long, priority: String) -> Unit
) {
    val context = LocalContext.current
    val defaultSubjectNames = com.example.domain.model.OFFICIAL_7TH_GRADE_SUBJECTS
    val subjectList = if (availableSubjects.isNotEmpty()) availableSubjects.map { it.name } else defaultSubjectNames

    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjectList.firstOrNull() ?: "Matemáticas") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.ALTA.code) }

    val priorities = listOf(
        TaskPriority.ALTA.code to "Alta",
        TaskPriority.MEDIA.code to "Media",
        TaskPriority.BAJA.code to "Baja"
    )

    val cal = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 2)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
        }
    }
    var selectedDueMillis by remember { mutableStateOf(cal.timeInMillis) }

    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("es", "ES"))
    val formattedDue = dateFormat.format(Date(selectedDueMillis)).replaceFirstChar { it.uppercase() }

    fun showDatePicker() {
        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedDueMillis }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 0)
                }
                selectedDueMillis = newCal.timeInMillis
            },
            currentCal.get(Calendar.YEAR),
            currentCal.get(Calendar.MONTH),
            currentCal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    val dueOptions = listOf(
        1 to "Mañana (24h)",
        2 to "En 2 días",
        3 to "En 3 días",
        5 to "En 5 días",
        7 to "En 1 semana"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la tarea") },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Materia:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjectList.forEach { sub ->
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

                Text("Fecha de Entrega:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f).clickable { showDatePicker() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedDue,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = { showDatePicker() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Elegir Fecha", fontSize = 11.sp)
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dueOptions.forEach { (days, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                val newCal = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, days)
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 0)
                                }
                                selectedDueMillis = newCal.timeInMillis
                            }
                        ) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📅 Vence: $formattedDue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔔 Notificación Push programada para 1 día antes a las 3:00 PM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Text("Prioridad:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    priorities.forEach { (key, label) ->
                        val isSelected = selectedPriority == key
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedPriority = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instrucciones (opcional)") },
                    placeholder = { Text("Detalles de la entrega...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, selectedSubject, description, selectedDueMillis, selectedPriority)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_task_button")
            ) {
                Text("Guardar Tarea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExamDialog(
    availableSubjects: List<SubjectEntity> = emptyList(),
    onDismiss: () -> Unit,
    onAdd: (title: String, subject: String, dateMillis: Long, classroom: String, topics: String) -> Unit
) {
    val context = LocalContext.current
    val defaultSubjectNames = com.example.domain.model.OFFICIAL_7TH_GRADE_SUBJECTS
    val subjectList = if (availableSubjects.isNotEmpty()) availableSubjects.map { it.name } else defaultSubjectNames

    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjectList.firstOrNull() ?: "Matemáticas") }
    var topics by remember { mutableStateOf("") }

    val cal = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
    }
    var selectedExamDateMillis by remember { mutableStateOf(cal.timeInMillis) }

    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM yyyy", Locale("es", "ES"))
    val formattedExamDate = dateFormat.format(Date(selectedExamDateMillis)).replaceFirstChar { it.uppercase() }

    fun showExamDatePicker() {
        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedExamDateMillis }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                selectedExamDateMillis = newCal.timeInMillis
            },
            currentCal.get(Calendar.YEAR),
            currentCal.get(Calendar.MONTH),
            currentCal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    val examDatePresets = listOf(
        3 to "En 3 días",
        7 to "En 1 semana",
        14 to "En 2 semanas"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Evaluación", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la evaluación") },
                    modifier = Modifier.fillMaxWidth().testTag("exam_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Materia:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjectList.forEach { sub ->
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

                Text("📅 Fecha del Examen:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f).clickable { showExamDatePicker() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedExamDate,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = { showExamDatePicker() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Elegir Fecha", fontSize = 11.sp)
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    examDatePresets.forEach { (days, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                val newCal = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, days)
                                    set(Calendar.HOUR_OF_DAY, 8)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                }
                                selectedExamDateMillis = newCal.timeInMillis
                            }
                        ) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = topics,
                    onValueChange = { topics = it },
                    label = { Text("Temario (opcional)") },
                    placeholder = { Text("Temas o capítulos principales...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, selectedSubject, selectedExamDateMillis, "", topics)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_exam_button")
            ) {
                Text("Agendar Examen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
