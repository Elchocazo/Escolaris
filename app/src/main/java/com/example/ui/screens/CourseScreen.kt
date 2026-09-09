package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.components.ExamScannerDialog
import com.example.ui.viewmodel.SchoolViewModel
import com.example.utils.CalendarSyncHelper

@Composable
fun CourseScreen(
    viewModel: SchoolViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allSchoolEvents by viewModel.allSchoolEvents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val isTeacher = UserRole.isTeacherOrAdmin(currentUser?.role) || currentUser?.email == "moz658@gmail.com"
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

    var selectedCourseTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    var weeklyScheduleMap by remember { mutableStateOf(OFFICIAL_SCHEDULE_MAP) }
    var editingClassSlot by remember { mutableStateOf<Triple<Int, Int, OfficialScheduleClassSlot?>?>(null) }
    var showAddEventDateMillis by remember { mutableStateOf<Long?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    val courseTabs = listOf(
        "⏰" to "Horario",
        "📅" to "Calendario",
        "📚" to "Tareas",
        "📝" to "Evaluaciones"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedCourseTab,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 16.dp,
                divider = {}
            ) {
                courseTabs.forEachIndexed { index, (emoji, label) ->
                    Tab(
                        selected = selectedCourseTab == index,
                        onClick = { selectedCourseTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontWeight = if (selectedCourseTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        }
                    )
                }
            }

            when (selectedCourseTab) {
                0 -> {
                    // TAB 0: HORARIO SEMANAL OFICIAL
                    OfficialWeeklyTimetableView(
                        weeklySchedule = weeklyScheduleMap,
                        canEdit = isTeacher,
                        onAddSlot = if (isTeacher) { { day -> editingClassSlot = Triple(day, -1, null) } } else null,
                        onEditSlot = if (isTeacher) { { day, index, slot -> editingClassSlot = Triple(day, index, slot) } } else null,
                        onDeleteSlot = if (isTeacher) { { day, index ->
                            val currentSlots = weeklyScheduleMap[day]?.toMutableList() ?: mutableListOf()
                            if (index in currentSlots.indices) {
                                currentSlots.removeAt(index)
                                weeklyScheduleMap = weeklyScheduleMap.toMutableMap().apply { put(day, currentSlots) }
                            }
                        } } else null
                    )
                }

                1 -> {
                    // TAB 1: CALENDARIO MENSUAL INSTITUCIONAL
                    MonthlyCalendarView(
                        tasks = visibleTasks,
                        exams = visibleExams,
                        schedules = schedules,
                        events = allSchoolEvents,
                        onAddTask = { if (!isParent) showAddTaskDialog = true },
                        onAddEvent = if (isTeacher) { { dateMillis -> showAddEventDateMillis = dateMillis } } else null,
                        onDeleteEvent = if (isTeacher) { { viewModel.deleteSchoolEvent(it) } } else null
                    )
                }

                2 -> {
                    // TAB 2: TAREAS ESCOLARES
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
                                            isTeacher -> "Tareas Asignadas"
                                            isParent -> "Tareas del Estudiante"
                                            else -> "Mis Tareas"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when {
                                            isTeacher -> "Publica y programa las tareas escolares para tus alumnos"
                                            isParent -> "Revisa las entregas y tareas pendientes de tu acudido"
                                            else -> "Gestiona tus tareas escolares y fechas de entrega"
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
                                            onClick = { showAddTaskDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .testTag("add_task_button")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isTeacher) "Asignar Tarea" else "Nueva Tarea",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }

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

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                3 -> {
                    // TAB 3: EVALUACIONES Y EXÁMENES
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isTeacher) {
                                            Button(
                                                onClick = { showAddExamDialog = true },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("add_exam_button")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Agendar Examen",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        FilledTonalButton(
                                            onClick = { showScannerDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("scan_exam_button")
                                        ) {
                                            Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isTeacher) "Escanear y Calificar" else "Escanear Examen",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }

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

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    if (editingClassSlot != null) {
        val (day, index, slot) = editingClassSlot!!
        EditClassSlotDialog(
            initialDay = day,
            initialSlot = slot,
            onDismiss = { editingClassSlot = null },
            onSave = { targetDay, updatedSlot ->
                val map = weeklyScheduleMap.toMutableMap()
                val list = map[targetDay]?.toMutableList() ?: mutableListOf()
                if (day == targetDay && index in list.indices) {
                    list[index] = updatedSlot
                } else {
                    list.add(updatedSlot)
                }
                map[targetDay] = list
                weeklyScheduleMap = map
                editingClassSlot = null
            }
        )
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
