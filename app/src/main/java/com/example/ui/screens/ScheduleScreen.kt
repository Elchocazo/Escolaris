package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Event
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.SchoolEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.schedules.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val schoolEvents by viewModel.allSchoolEvents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isTeacher = currentUser?.role == com.example.domain.model.UserRole.TEACHER.code
    val isParent = currentUser?.role == com.example.domain.model.UserRole.PARENT.code
    val isParentLinked = isParent && !currentUser?.linkedStudentId.isNullOrBlank()

    val visibleTasks = remember(tasks, isParent, isParentLinked, currentUser) {
        if (isParent) (if (isParentLinked) tasks.filter { it.studentId == currentUser?.linkedStudentId } else emptyList()) else tasks
    }
    val visibleExams = remember(exams, isParent, isParentLinked, currentUser) {
        if (isParent) (if (isParentLinked) exams.filter { it.studentId == currentUser?.linkedStudentId } else emptyList()) else exams
    }

    var selectedMainTab by remember { mutableIntStateOf(0) } // 0 = Horario de Clases, 1 = Calendario Mensual, 2 = Directorio & Atención
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showAddEventDateMillis by remember { mutableStateOf<Long?>(null) }

    // Dynamic state for official schedule and teacher directory
    var teacherDirectoryList by remember { mutableStateOf(OFFICIAL_TEACHER_DIRECTORY) }
    var weeklyScheduleMap by remember { mutableStateOf(OFFICIAL_SCHEDULE_MAP) }

    var editingTeacher by remember { mutableStateOf<Pair<Int, TeacherDirectoryEntry?>?>(null) }
    var editingClassSlot by remember { mutableStateOf<Triple<Int, Int, OfficialScheduleClassSlot?>?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("⏰", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Horario",
                                fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("📅", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Calendario",
                                fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedMainTab == 2,
                    onClick = { selectedMainTab = 2 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("📞", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Directorio",
                                fontWeight = if (selectedMainTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )
            }

            when (selectedMainTab) {
                0 -> {
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
                    MonthlyCalendarView(
                        tasks = visibleTasks,
                        exams = visibleExams,
                        schedules = schedules,
                        events = schoolEvents,
                        onAddTask = if (!isParent) { { viewModel.addNewTask("Nueva Tarea", "Materia General", "", System.currentTimeMillis(), "MEDIA") } } else null,
                        onAddEvent = if (isTeacher) { { dateMillis -> showAddEventDateMillis = dateMillis } } else null,
                        onDeleteEvent = if (isTeacher) { { viewModel.deleteSchoolEvent(it) } } else null
                    )
                }
                2 -> {
                    TeacherDirectoryView(
                        teachers = teacherDirectoryList,
                        canEdit = isTeacher,
                        onAddTeacher = if (isTeacher) { { editingTeacher = Pair(-1, null) } } else null,
                        onEditTeacher = if (isTeacher) { { index, teacher -> editingTeacher = Pair(index, teacher) } } else null,
                        onDeleteTeacher = if (isTeacher) { { index ->
                            if (index in teacherDirectoryList.indices) {
                                teacherDirectoryList = teacherDirectoryList.toMutableList().apply { removeAt(index) }
                            }
                        } } else null
                    )
                }
            }
        }
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

    if (showAddScheduleDialog) {
        AddScheduleSlotDialog(
            onDismiss = { showAddScheduleDialog = false },
            onAdd = { day, start, end, subj, room, teacher, color, selfStudy, notes ->
                viewModel.addScheduleSlot(day, start, end, subj, room, teacher, color, selfStudy, notes)
            }
        )
    }
}

private fun isEventOnDate(event: SchoolEventEntity, year: Int, month: Int, day: Int): Boolean {
    val dayStartCal = Calendar.getInstance().apply {
        set(year, month, day, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val dayEndCal = Calendar.getInstance().apply {
        set(year, month, day, 23, 59, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val eventEnd = if (event.endDateMillis > 0L) event.endDateMillis else event.eventDateMillis
    return event.eventDateMillis <= dayEndCal.timeInMillis && eventEnd >= dayStartCal.timeInMillis
}

@Composable
fun MonthlyCalendarView(
    tasks: List<TaskEntity>,
    exams: List<ExamEntity>,
    schedules: List<ScheduleEntity>,
    events: List<SchoolEventEntity> = emptyList(),
    onAddTask: (() -> Unit)? = null,
    onAddEvent: ((Long) -> Unit)? = null,
    onDeleteEvent: ((SchoolEventEntity) -> Unit)? = null
) {
    var calendarMonth by remember {
        mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) })
    }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    val currentYear = calendarMonth.get(Calendar.YEAR)
    val currentMonth = calendarMonth.get(Calendar.MONTH)
    val monthName = remember(calendarMonth) {
        val format = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        format.format(calendarMonth.time).replaceFirstChar { it.uppercase() }
    }

    val daysOfWeekLabels = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")

    val gridDays = remember(currentYear, currentMonth) {
        val cal = calendarMonth.clone() as Calendar
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val maxDaysPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val daysList = mutableListOf<CalendarDayInfo>()
        for (i in 0 until dayOffset) {
            val dayNum = maxDaysPrevMonth - dayOffset + 1 + i
            daysList.add(CalendarDayInfo(dayNum, isCurrentMonth = false, isPrevMonth = true))
        }
        for (d in 1..maxDaysInMonth) {
            daysList.add(CalendarDayInfo(d, isCurrentMonth = true, isPrevMonth = false))
        }
        val total = if (daysList.size <= 35) 35 else 42
        var nextDay = 1
        while (daysList.size < total) {
            daysList.add(CalendarDayInfo(nextDay++, isCurrentMonth = false, isPrevMonth = false))
        }
        daysList
    }

    val selectedDateCal = remember(selectedDay, currentMonth, currentYear) {
        Calendar.getInstance().apply { set(currentYear, currentMonth, selectedDay) }
    }
    val selectedDayOfWeek = remember(selectedDateCal) {
        val dow = selectedDateCal.get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.SUNDAY) 7 else dow - 1
    }

    val dayTasks = remember(tasks, selectedDay, currentMonth, currentYear) {
        tasks.filter {
            val tCal = Calendar.getInstance().apply { timeInMillis = it.dueDateMillis }
            tCal.get(Calendar.YEAR) == currentYear && tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
    }

    val dayExams = remember(exams, selectedDay, currentMonth, currentYear) {
        exams.filter {
            val eCal = Calendar.getInstance().apply { timeInMillis = it.examDateMillis }
            eCal.get(Calendar.YEAR) == currentYear && eCal.get(Calendar.MONTH) == currentMonth && eCal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
    }

    val dayEvents = remember(events, selectedDay, currentMonth, currentYear) {
        events.filter { isEventOnDate(it, currentYear, currentMonth, selectedDay) }
    }

    val dayClasses = remember(schedules, selectedDayOfWeek) {
        schedules.filter { it.dayOfWeek == selectedDayOfWeek }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val today = Calendar.getInstance()
                                calendarMonth = today.clone() as Calendar
                                selectedDay = today.get(Calendar.DAY_OF_MONTH)
                            }
                    ) {
                        Text(
                            text = "Hoy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val prev = calendarMonth.clone() as Calendar
                                prev.add(Calendar.MONTH, -1)
                                calendarMonth = prev
                            }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", modifier = Modifier.padding(6.dp).size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val next = calendarMonth.clone() as Calendar
                                next.add(Calendar.MONTH, 1)
                                calendarMonth = next
                            }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", modifier = Modifier.padding(6.dp).size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        daysOfWeekLabels.forEach { label ->
                            Text(
                                text = label.take(3),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val rows = gridDays.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 1.5.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            week.forEach { dayInfo ->
                                val isSelected = dayInfo.isCurrentMonth && dayInfo.dayNumber == selectedDay
                                val isToday = dayInfo.isCurrentMonth &&
                                        dayInfo.dayNumber == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                        currentMonth == Calendar.getInstance().get(Calendar.MONTH) &&
                                        currentYear == Calendar.getInstance().get(Calendar.YEAR)

                                val hasTasks = if (dayInfo.isCurrentMonth) {
                                    tasks.any {
                                        val tCal = Calendar.getInstance().apply { timeInMillis = it.dueDateMillis }
                                        tCal.get(Calendar.DAY_OF_MONTH) == dayInfo.dayNumber &&
                                        tCal.get(Calendar.MONTH) == currentMonth &&
                                        tCal.get(Calendar.YEAR) == currentYear
                                    }
                                } else false

                                val hasExams = if (dayInfo.isCurrentMonth) {
                                    exams.any {
                                        val eCal = Calendar.getInstance().apply { timeInMillis = it.examDateMillis }
                                        eCal.get(Calendar.DAY_OF_MONTH) == dayInfo.dayNumber &&
                                        eCal.get(Calendar.MONTH) == currentMonth &&
                                        eCal.get(Calendar.YEAR) == currentYear
                                    }
                                } else false

                                val hasEvents = if (dayInfo.isCurrentMonth) {
                                    events.any { isEventOnDate(it, currentYear, currentMonth, dayInfo.dayNumber) }
                                } else false

                                val isContinuousRange = if (dayInfo.isCurrentMonth) {
                                    events.any {
                                        it.endDateMillis > it.eventDateMillis && isEventOnDate(it, currentYear, currentMonth, dayInfo.dayNumber)
                                    }
                                } else false

                                val cellBg = when {
                                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                    isContinuousRange -> Color(0xFFF3E8FF).copy(alpha = 0.75f)
                                    dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.surface
                                    else -> Color(0xFFF8FAFC)
                                }

                                val cellBorder = when {
                                    isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                    isContinuousRange -> BorderStroke(0.8.dp, Color(0xFFD8B4FE).copy(alpha = 0.8f))
                                    else -> null
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .then(
                                            if (cellBorder != null) Modifier.border(cellBorder, RoundedCornerShape(6.dp))
                                            else Modifier
                                        )
                                        .background(cellBg)
                                        .clickable(enabled = dayInfo.isCurrentMonth) {
                                            selectedDay = dayInfo.dayNumber
                                        }
                                        .padding(horizontal = 2.dp, vertical = 3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (dayInfo.dayNumber > 0) "${dayInfo.dayNumber}" else "",
                                            fontSize = 11.sp,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (!dayInfo.isCurrentMonth) Color(0xFFCBD5E1)
                                            else if (isToday) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.align(Alignment.Start).padding(start = 2.dp)
                                        )

                                        if (hasTasks || hasExams || hasEvents) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (hasEvents) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF7E22CE))
                                                    )
                                                }
                                                if (hasExams) {
                                                    if (hasEvents) Spacer(modifier = Modifier.width(3.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFEA580C))
                                                    )
                                                }
                                                if (hasTasks) {
                                                    if (hasEvents || hasExams) Spacer(modifier = Modifier.width(3.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF16A34A))
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
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Actividades del $selectedDay de $monthName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (onAddEvent != null) {
                    FilledTonalIconButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                set(currentYear, currentMonth, selectedDay, 8, 0)
                            }
                            onAddEvent(cal.timeInMillis)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Evento",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (dayEvents.isNotEmpty()) {
            items(dayEvents, key = { "ev_${it.id}" }) { event ->
                CalendarEventCard(
                    event = event,
                    onDelete = if (onDeleteEvent != null) { { onDeleteEvent(event) } } else null
                )
            }
        }

        if (dayClasses.isNotEmpty()) {
            items(dayClasses, key = { "sched_${it.id}" }) { schedule ->
                ScheduleSlotCard(slot = schedule, onDelete = null)
            }
        }

        if (dayExams.isNotEmpty()) {
            items(dayExams, key = { "exam_${it.id}" }) { exam ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.dp, Color(0xFFFDBA74)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✏️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("EXAMEN: ${exam.title}", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                            Text("Materia: ${exam.subject} • Aula: ${exam.classroom}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (dayTasks.isNotEmpty()) {
            items(dayTasks, key = { "task_${it.id}" }) { task ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📚", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("TAREA: ${task.title}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Materia: ${task.subject} • Prioridad: ${task.priority}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (dayClasses.isEmpty() && dayTasks.isEmpty() && dayExams.isEmpty() && dayEvents.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sin actividades para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (onAddEvent != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        set(currentYear, currentMonth, selectedDay, 8, 0)
                                    }
                                    onAddEvent(cal.timeInMillis)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Programar Evento", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun CalendarEventCard(
    event: SchoolEventEntity,
    onDelete: (() -> Unit)? = null
) {
    val categoryLabel = when (event.category.uppercase()) {
        "ACADEMIC" -> "Académico"
        "COMMUNITY" -> "Padres"
        "CIVIC" -> "Cívico"
        "CULTURAL" -> "Cultural"
        "CAFETERIA" -> "Cafetería"
        "INSTITUTIONAL" -> "Institucional"
        else -> event.category
    }

    val categoryColor = when (event.category.uppercase()) {
        "ACADEMIC" -> Color(0xFFDC2626)
        "COMMUNITY" -> Color(0xFF7C3AED)
        "CIVIC" -> Color(0xFF2563EB)
        "CULTURAL" -> Color(0xFFDB2777)
        "CAFETERIA" -> Color(0xFFD97706)
        else -> Color(0xFF059669)
    }

    val categoryBg = when (event.category.uppercase()) {
        "ACADEMIC" -> Color(0xFFFEE2E2)
        "COMMUNITY" -> Color(0xFFF3E8FF)
        "CIVIC" -> Color(0xFFDBEAFE)
        "CULTURAL" -> Color(0xFFFCE7F3)
        "CAFETERIA" -> Color(0xFFFEF3C7)
        else -> Color(0xFFD1FAE5)
    }

    val eventEmoji = when (event.category.uppercase()) {
        "ACADEMIC" -> "📝"
        "COMMUNITY" -> "👨‍👩‍👧"
        "CIVIC" -> "📢"
        "CULTURAL" -> "⭐"
        "CAFETERIA" -> "🍔"
        else -> "🎒"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFBF8FF),
        border = BorderStroke(1.dp, Color(0xFFD8B4FE)),
        shadowElevation = 1.dp,
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(categoryBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(eventEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF581C87),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(end = 6.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = categoryBg
                        ) {
                            Text(
                                text = categoryLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (event.eventTime.isNotBlank() || event.location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (event.eventTime.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFF7E22CE),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = event.eventTime,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF7E22CE)
                                )
                                if (event.location.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }

                            if (event.location.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = event.location,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val cleanDesc = event.description.trim()
                    val isRedundantTime = cleanDesc.startsWith("Hora", ignoreCase = true) ||
                            cleanDesc.equals(event.eventTime, ignoreCase = true)
                    if (cleanDesc.isNotBlank() && !isRedundantTime) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = cleanDesc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar evento",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCalendarEventDialog(
    initialDateMillis: Long,
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, dateMillis: Long, time: String, location: String, desc: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Auditorio Principal") }

    val categories = listOf(
        "ACADÉMICO" to "🎓 Académico",
        "CÍVICO" to "🇨🇴 Cívico / Izada",
        "DEPORTIVO" to "⚽ Deportivo",
        "CULTURAL" to "🎭 Cultural / Arte",
        "INSTITUCIONAL" to "🏫 Institucional"
    )
    var selectedCategory by remember { mutableStateOf(categories.first().first) }

    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var formattedSelectedTime by remember { mutableStateOf("08:30 AM") }

    fun showTimePicker() {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                val isPm = hourOfDay >= 12
                val hour12 = when {
                    hourOfDay == 0 -> 12
                    hourOfDay > 12 -> hourOfDay - 12
                    else -> hourOfDay
                }
                val amPm = if (isPm) "PM" else "AM"
                formattedSelectedTime = String.format("%02d:%02d %s", hour12, minute, amPm)
            },
            selectedHour,
            selectedMinute,
            false
        ).show()
    }

    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM yyyy", Locale("es", "ES"))
    val formattedDate = dateFormat.format(Date(initialDateMillis)).replaceFirstChar { it.uppercase() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Agendar Evento Escolar", fontWeight = FontWeight.Bold)
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del evento") },
                    placeholder = { Text("Ej: Feria de la Ciencia") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Categoría:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { (catCode, catLabel) ->
                        val isSel = selectedCategory == catCode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF7E22CE) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = catCode }
                        ) {
                            Text(
                                text = catLabel,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text("⏰ Selector de Hora:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePicker() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formattedSelectedTime,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = { showTimePicker() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Elegir Hora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lugar (opcional)") },
                    placeholder = { Text("Ej: Auditorio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, selectedCategory, initialDateMillis, formattedSelectedTime, location, "")
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Agendar Evento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ScheduleSlotCard(slot: ScheduleEntity, onDelete: (() -> Unit)?) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = slot.subject,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (slot.teacherOrTutor.isNotBlank() || slot.classroomOrLocation.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (slot.teacherOrTutor.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = slot.teacherOrTutor,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (slot.teacherOrTutor.isNotBlank() && slot.classroomOrLocation.isNotBlank()) {
                        Text(" • ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    if (slot.classroomOrLocation.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = slot.classroomOrLocation,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddScheduleSlotDialog(
    onDismiss: () -> Unit,
    onAdd: (Int, String, String, String, String, String, Long, Boolean, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableIntStateOf(1) }
    var teacher by remember { mutableStateOf("") }
    var classroom by remember { mutableStateOf("Aula 101") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Bloque Personalizado", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Docente") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("Aula") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(dayOfWeek, "07:00", "08:30", subject.ifBlank { "Clase" }, classroom, teacher.ifBlank { "Docente" }, 0xFF2563EB, false, "")
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private data class CalendarDayInfo(val dayNumber: Int, val isCurrentMonth: Boolean, val isPrevMonth: Boolean)

data class OfficialScheduleClassSlot(
    val timeSlot: String,
    val subject: String,
    val teacher: String,
    val classroom: String = "",
    val iconEmoji: String,
    val isBreak: Boolean = false
)

data class TeacherDirectoryEntry(
    val name: String,
    val roleOrGrade: String,
    val subject: String,
    val email: String?,
    val attentionDay: String,
    val attentionHours: String,
    val avatarEmoji: String,
    val subjectsIn7th: String = ""
)

val OFFICIAL_SCHEDULE_MAP: Map<Int, List<OfficialScheduleClassSlot>> = mapOf(
    1 to listOf( // LUNES
        OfficialScheduleClassSlot("7:00 - 7:50", "Dirección de grupo", "Manuel Muñoz", "", "👨‍🏫"),
        OfficialScheduleClassSlot("7:50 - 8:40", "Tecnología e Informática", "Manuel Muñoz", "", "💻"),
        OfficialScheduleClassSlot("8:40 - 9:30", "Biología", "Anna Fulí", "", "🔬"),
        OfficialScheduleClassSlot("9:30 - 10:10", "Descanso", "", "", "🥪", isBreak = true),
        OfficialScheduleClassSlot("10:10 - 11:00", "Inglés", "Ángela Rendón", "", "🇬🇧"),
        OfficialScheduleClassSlot("11:00 - 11:50", "Ciencias Sociales", "Ibón Ocampo", "", "🌍"),
        OfficialScheduleClassSlot("11:50 - 12:35", "Español", "Katherine Castro", "", "📖"),
        OfficialScheduleClassSlot("12:35 - 1:20", "Español", "Katherine Castro", "", "📖")
    ),
    2 to listOf( // MARTES
        OfficialScheduleClassSlot("7:00 - 7:50", "Geometría", "Carlos Erazo", "", "📐"),
        OfficialScheduleClassSlot("7:50 - 8:40", "Matemáticas", "Manuel Muñoz", "", "➕"),
        OfficialScheduleClassSlot("8:40 - 9:30", "Matemáticas", "Manuel Muñoz", "", "➕"),
        OfficialScheduleClassSlot("9:30 - 10:10", "Descanso", "", "", "🥪", isBreak = true),
        OfficialScheduleClassSlot("10:10 - 11:00", "Estética", "Sirley Palta", "", "🎨"),
        OfficialScheduleClassSlot("11:00 - 11:50", "Biología", "Anna Fulí", "", "🔬"),
        OfficialScheduleClassSlot("11:50 - 12:35", "Música", "Danilo Daza", "", "🎵"),
        OfficialScheduleClassSlot("12:35 - 1:20", "Ciencias Sociales", "Ibón Ocampo", "", "🌍")
    ),
    3 to listOf( // MIÉRCOLES
        OfficialScheduleClassSlot("7:00 - 7:50", "Cátedra emocional", "Irnalda Tintinago", "", "💛"),
        OfficialScheduleClassSlot("7:50 - 8:40", "Ciencias Sociales", "Ibón Ocampo", "", "🌍"),
        OfficialScheduleClassSlot("8:40 - 9:30", "Física", "Víctor Toro", "", "⚡"),
        OfficialScheduleClassSlot("9:30 - 10:10", "Descanso", "", "", "🥪", isBreak = true),
        OfficialScheduleClassSlot("10:10 - 11:00", "Español", "Katherine Castro", "", "📖"),
        OfficialScheduleClassSlot("11:00 - 11:50", "Matemáticas", "Manuel Muñoz", "", "➕"),
        OfficialScheduleClassSlot("11:50 - 12:35", "Matemáticas", "Manuel Muñoz", "", "➕"),
        OfficialScheduleClassSlot("12:35 - 1:20", "Educación Física", "José Benavides", "", "⚽")
    ),
    4 to listOf( // JUEVES
        OfficialScheduleClassSlot("7:00 - 7:50", "Inglés", "Ángela Rendón", "", "🇬🇧"),
        OfficialScheduleClassSlot("7:50 - 8:40", "Ética", "Irnalda Tintinago", "", "🤝"),
        OfficialScheduleClassSlot("8:40 - 9:30", "Química", "Anna Fulí", "", "🧪"),
        OfficialScheduleClassSlot("9:30 - 10:10", "Descanso", "", "", "🥪", isBreak = true),
        OfficialScheduleClassSlot("10:10 - 11:00", "Química", "Anna Fulí", "", "🧪"),
        OfficialScheduleClassSlot("11:00 - 11:50", "Ciencias Sociales", "Ibón Ocampo", "", "🌍"),
        OfficialScheduleClassSlot("11:50 - 12:35", "Religión", "Mónica García", "", "🕊️"),
        OfficialScheduleClassSlot("12:35 - 1:20", "Español", "Katherine Castro", "", "📖")
    ),
    5 to listOf( // VIERNES
        OfficialScheduleClassSlot("7:00 - 7:50", "Inglés", "Ángela Rendón", "", "🇬🇧"),
        OfficialScheduleClassSlot("7:50 - 8:40", "Razonamiento Matemático", "Víctor Toro", "", "🔢"),
        OfficialScheduleClassSlot("8:40 - 9:30", "Español", "Katherine Castro", "", "📖"),
        OfficialScheduleClassSlot("9:30 - 10:10", "Descanso", "", "", "🥪", isBreak = true),
        OfficialScheduleClassSlot("10:10 - 11:00", "Biología", "Anna Fulí", "", "🔬"),
        OfficialScheduleClassSlot("11:00 - 11:50", "Biología", "Anna Fulí", "", "🔬"),
        OfficialScheduleClassSlot("11:50 - 12:35", "Matemáticas", "Manuel Muñoz", "", "➕"),
        OfficialScheduleClassSlot("12:35 - 1:20", "Dirección de grupo", "Manuel Muñoz", "", "👨‍🏫")
    )
)

val OFFICIAL_TEACHER_DIRECTORY = listOf(
    TeacherDirectoryEntry("Hna. Myriam Marmolejo", "Rectora", "Rectoría & Dirección General", null, "Lunes a Viernes", "9:00 AM a 12:00 M", "👩‍💼", ""),
    TeacherDirectoryEntry("Marisol Ruiz", "Coordinadora Académica", "Coordinación Académica & Pedagógica", "coordinacionacademica@colegiohogarmadrededios.edu.co", "Lunes a Jueves", "2:00 PM a 4:00 PM", "👩‍🏫", ""),
    TeacherDirectoryEntry("Irnalda Tintinago", "Docente & Psicóloga", "Cátedra emocional, Ética", "irnalda.tintinago@colegiohogarmadrededios.edu.co", "Miércoles", "12:30 PM - 1:20 PM", "💛", "Cátedra emocional, Ética"),
    TeacherDirectoryEntry("Sirley Palta", "Director de grado Transición", "Estética", "anyi.palta@colegiohogarmadrededios.edu.co", "Lunes", "12:30 PM - 1:20 PM", "🎨", "Estética"),
    TeacherDirectoryEntry("Martha Campo", "Director de grado 1°", "Docente Titular Primaria", null, "Martes", "1:20 PM - 2:00 PM", "👩‍🏫", ""),
    TeacherDirectoryEntry("Mónica García", "Director de grado 2°", "Religión", "monica.garcia@colegiohogarmadrededios.edu.co", "Jueves", "12:30 PM - 1:20 PM", "🕊️", "Religión"),
    TeacherDirectoryEntry("Danilo Daza", "Director de grado 3°", "Música", "danilo.daza@colegiohogarmadrededios.edu.co", "Miércoles", "12:30 PM - 1:20 PM", "🎵", "Música"),
    TeacherDirectoryEntry("Ángela Rendón", "Director de grado 4A", "Inglés", "angela.rendon@colegiohogarmadrededios.edu.co", "Jueves", "1:20 PM - 2:00 PM", "🇬🇧", "Inglés"),
    TeacherDirectoryEntry("Katherine Castro", "Director de grado 4B", "Español", "Katherine.castro@colegiohogarmadrededios.edu.co", "Jueves", "1:20 PM - 2:00 PM", "📖", "Español"),
    TeacherDirectoryEntry("María Rodríguez", "Director de grado 5°", "Docente Titular Primaria", null, "Martes", "12:30 PM - 1:20 PM", "👩‍🏫", ""),
    TeacherDirectoryEntry("Ibón Ocampo", "Director de grado 6°", "Ciencias Sociales", "ibon.ocampo@colegiohogarmadrededios.edu.co", "Martes", "1:20 PM - 2:00 PM", "🌍", "Ciencias Sociales"),
    TeacherDirectoryEntry("Manuel Muñoz", "Director de grado 7°", "Matemáticas, Tecnología e Informática", "nformaticachmd@colegiohogarmadrededios.edu.co", "Miércoles", "12:30 PM - 1:20 PM", "👨‍🏫", "Matemáticas, Tecnología e Informática"),
    TeacherDirectoryEntry("José Benavides", "Director de grado 8°", "Educación Física", "jose.benavides@colegiohogarmadrededios.edu.co", "Martes", "12:30 PM - 1:20 PM", "⚽", "Educación Física"),
    TeacherDirectoryEntry("Víctor Toro", "Director de grado 9°", "Física, Razonamiento Matemático", "victor.toro@colegiohogarmadrededios.edu.co", "Jueves", "1:20 PM - 2:00 PM", "⚡", "Física, Razonamiento Matemático"),
    TeacherDirectoryEntry("Carlos Erazo", "Director de grado 10°", "Geometría", "carlos.erazo@colegiohogarmadrededios.edu.co", "Lunes", "12:30 PM - 1:20 PM", "📐", "Geometría"),
    TeacherDirectoryEntry("Anna Fulí", "Director de grado 11°", "Biología, Química", "ana.fuli@colegiohogarmadrededios.edu.co", "Miércoles", "12:30 PM - 1:20 PM", "🔬", "Biología, Química")
)

fun launchTeacherGmail(context: android.content.Context, email: String, teacherName: String) {
    try {
        val subject = Uri.encode("Consulta Institucional - Colegio Hogar Madre de Dios")
        val body = Uri.encode("Estimado/a $teacherName,\n\nLe escribo cordialmente para realizar la siguiente consulta:\n\n\nAtentamente,\nAcudiente / Estudiante")
        val mailtoUri = Uri.parse("mailto:$email?subject=$subject&body=$body")
        val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri)
        
        emailIntent.setPackage("com.google.android.gm")
        try {
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SENDTO, mailtoUri)
            context.startActivity(Intent.createChooser(fallbackIntent, "Enviar correo a $teacherName"))
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No se pudo abrir la app de correo: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun OfficialWeeklyTimetableView(
    weeklySchedule: Map<Int, List<OfficialScheduleClassSlot>> = OFFICIAL_SCHEDULE_MAP,
    canEdit: Boolean = false,
    onAddSlot: ((Int) -> Unit)? = null,
    onEditSlot: ((Int, Int, OfficialScheduleClassSlot) -> Unit)? = null,
    onDeleteSlot: ((Int, Int) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedDayOfWeek by remember { mutableIntStateOf(1) } // 1 = Lun, 2 = Mar, 3 = Mié, 4 = Jue, 5 = Vie
    val daysOfWeek = listOf(1 to "Lun", 2 to "Mar", 3 to "Mié", 4 to "Jue", 5 to "Vie")
    val currentDayClasses = weeklySchedule[selectedDayOfWeek] ?: emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Horario 7°",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "• 7:00 AM - 1:20 PM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                FilledTonalIconButton(
                    onClick = {
                        com.example.utils.PdfExporter.generateAndShareWeeklySchedule(context, weeklySchedule)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Descargar PDF Horario",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                daysOfWeek.forEach { (num, label) ->
                    val isSelected = selectedDayOfWeek == num
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedDayOfWeek = num }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        if (currentDayClasses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay clases registradas para este día.",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(currentDayClasses.size) { index ->
                val slot = currentDayClasses[index]
                if (slot.isBreak) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        shadowElevation = 0.5.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(slot.iconEmoji.ifBlank { "🥪" }, fontSize = 17.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = slot.subject,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = Color(0xFF92400E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = slot.timeSlot,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    if (canEdit) {
                                        IconButton(
                                            onClick = { onEditSlot?.invoke(selectedDayOfWeek, index, slot) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFB45309), modifier = Modifier.size(15.dp))
                                        }
                                        IconButton(
                                            onClick = { onDeleteSlot?.invoke(selectedDayOfWeek, index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFDC2626), modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(slot.iconEmoji.ifBlank { "📚" }, fontSize = 17.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = slot.subject,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = slot.timeSlot,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    if (canEdit) {
                                        IconButton(
                                            onClick = { onEditSlot?.invoke(selectedDayOfWeek, index, slot) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                        }
                                        IconButton(
                                            onClick = { onDeleteSlot?.invoke(selectedDayOfWeek, index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFDC2626), modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }

                            if (slot.teacher.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 44.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Prof. ${slot.teacher}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
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

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun TeacherDirectoryView(
    teachers: List<TeacherDirectoryEntry> = OFFICIAL_TEACHER_DIRECTORY,
    canEdit: Boolean = false,
    onAddTeacher: (() -> Unit)? = null,
    onEditTeacher: ((Int, TeacherDirectoryEntry) -> Unit)? = null,
    onDeleteTeacher: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTeacherDetail by remember { mutableStateOf<Pair<Int, TeacherDirectoryEntry>?>(null) }

    // Dialog para ver la tarjeta en grande con todos los detalles al hacer click
    selectedTeacherDetail?.let { (idx, teacher) ->
        TeacherDetailDialog(
            teacher = teacher,
            onDismiss = { selectedTeacherDetail = null },
            onEdit = if (canEdit && onEditTeacher != null) { { onEditTeacher(idx, teacher) } } else null,
            onDelete = if (canEdit && onDeleteTeacher != null) { { onDeleteTeacher(idx) } } else null
        )
    }

    val filteredTeachersWithIndex = remember(searchQuery, teachers) {
        if (searchQuery.isBlank()) {
            teachers.mapIndexed { index, teacher -> index to teacher }
        } else {
            val q = searchQuery.trim().lowercase()
            teachers.mapIndexed { index, teacher -> index to teacher }.filter { (_, teacher) ->
                teacher.name.lowercase().contains(q) ||
                teacher.subject.lowercase().contains(q) ||
                teacher.roleOrGrade.lowercase().contains(q) ||
                (teacher.email?.lowercase()?.contains(q) == true) ||
                teacher.attentionDay.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Title Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏫", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Directorio Docente",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${teachers.size} docentes institucionales • 7° Grado",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dedicated Action Buttons Row (Generous spacing, never touching text, never wrapping vertically)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                com.example.utils.PdfExporter.generateAndShareTeacherDirectory(context, teachers)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFEDE9FE),
                                contentColor = Color(0xFF6D28D9)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Descargar PDF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (canEdit && onAddTeacher != null) {
                            Button(
                                onClick = onAddTeacher,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Agregar Docente",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca cualquier tarjeta para verla en grande con todos sus datos o enviar correo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por docente, materia o grado...", fontSize = 12.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (filteredTeachersWithIndex.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No se encontraron docentes con ese criterio de búsqueda.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        if (canEdit && onAddTeacher != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onAddTeacher) {
                                Text("+ Agregar Docente")
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredTeachersWithIndex, key = { it.second.name + it.first }) { (originalIndex, teacher) ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.2.dp, Color(0xFF7C3AED).copy(alpha = 0.35f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTeacherDetail = originalIndex to teacher }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // TOP ROW: Avatar + Nombre + Botones de Acción Rápida
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
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

                                Column {
                                    Text(
                                        text = teacher.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Materia: ${teacher.subject}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Botones de acción rápida: Email + Editar + Eliminar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (teacher.email != null) {
                                    IconButton(
                                        onClick = { launchTeacherGmail(context, teacher.email, teacher.name) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Enviar correo a ${teacher.name}",
                                            tint = Color(0xFFEA4335),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                if (canEdit) {
                                    IconButton(
                                        onClick = { onEditTeacher?.invoke(originalIndex, teacher) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar docente",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteTeacher?.invoke(originalIndex) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar docente",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // INSIGNIA DE ROL / GRADO CON ANCHO COMPLETO (Nunca se corta ni se tapa)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEDE9FE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎓", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = teacher.roleOrGrade,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6B21A8)
                                )
                            }
                        }

                        // INSIGNIA DE CLASES / MATERIAS QUE DICTA EN 7° GRADO (Para los Padres - Solo si aplica)
                        if (teacher.subjectsIn7th.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📖", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "En 7°: ${teacher.subjectsIn7th}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Horario de Atención Presencial (Ancho completo)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🗓️", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Atención Presencial: ${teacher.attentionDay} (${teacher.attentionHours})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        // Correo Institucional
                        if (teacher.email != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Correo: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = teacher.email,
                                    fontSize = if (teacher.email.length > 30) 10.5.sp else 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Atención: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Presencial en sede institucional",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Toca para ver en detalle 🔍",
                                fontSize = 10.sp,
                                color = Color(0xFF7C3AED),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun TeacherDetailDialog(
    teacher: TeacherDirectoryEntry,
    onDismiss: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    BackHandler {
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Volver / Minimizar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra superior con botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle del Docente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Avatar Gigante
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEDE9FE),
                    border = BorderStroke(2.5.dp, Color(0xFF7C3AED).copy(alpha = 0.5f)),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(teacher.avatarEmoji, fontSize = 38.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nombre Completo
                Text(
                    text = teacher.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Insignia de Rol / Grado
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEDE9FE)
                ) {
                    Text(
                        text = "🎓 ${teacher.roleOrGrade}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6B21A8),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Tarjeta Destacada de Materia(s) en Grado 7° (Para el Padre de Familia - Solo si aplica)
                if (teacher.subjectsIn7th.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.2.dp, Color(0xFF93C5FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📖", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Materia(s) que dicta a Grado 7°:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = teacher.subjectsIn7th,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjeta de Área / Especialidad Docente
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📚", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Área General / Especialidad:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = teacher.subject,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjeta de Atención Presencial
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗓️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Atención Presencial a Padres:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "${teacher.attentionDay} (${teacher.attentionHours})",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjeta de Correo Institucional
                if (teacher.email != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✉️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Correo Institucional:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEE2E2).copy(alpha = 0.6f))
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(teacher.email))
                                        android.widget.Toast.makeText(context, "Correo copiado al portapapeles 📋", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = teacher.email,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (teacher.email.length > 32) 10.5.sp else 11.5.sp,
                                    color = Color(0xFFB91C1C),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { launchTeacherGmail(context, teacher.email, teacher.name) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Abrir Gmail", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(teacher.email))
                                        android.widget.Toast.makeText(context, "Correo copiado al portapapeles 📋", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("Copiar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏫", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Atención presencial en la sede del colegio.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Acciones de administración
                if (onEdit != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                onDismiss()
                                onEdit()
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar Datos", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        TextButton(
                            onClick = {
                                onDismiss()
                                onDelete()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EditTeacherDialog(
    initialTeacher: TeacherDirectoryEntry?,
    onDismiss: () -> Unit,
    onSave: (TeacherDirectoryEntry) -> Unit
) {
    var name by remember { mutableStateOf(initialTeacher?.name ?: "") }
    var role by remember { mutableStateOf(initialTeacher?.roleOrGrade ?: "Docente Titular") }
    var subject by remember { mutableStateOf(initialTeacher?.subject ?: "") }
    var subjectsIn7th by remember { mutableStateOf(initialTeacher?.subjectsIn7th ?: "") }
    var email by remember { mutableStateOf(initialTeacher?.email ?: "") }
    var attentionDay by remember { mutableStateOf(initialTeacher?.attentionDay ?: "Lunes a Viernes") }
    var attentionHours by remember { mutableStateOf(initialTeacher?.attentionHours ?: "12:30 PM - 1:20 PM") }
    var emoji by remember { mutableStateOf(initialTeacher?.avatarEmoji ?: "👨‍🏫") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTeacher != null) "Editar Docente" else "Agregar Docente", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Docente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Cargo / Grado") },
                        modifier = Modifier.weight(1.5f)
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Emoji") },
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(
                    value = subjectsIn7th,
                    onValueChange = { subjectsIn7th = it },
                    label = { Text("Clase(s) que dicta a 7° Grado") },
                    placeholder = { Text("Ej: Matemáticas y Sistemas") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Área General / Especialidad") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Institucional (Opcional)") },
                    placeholder = { Text("Dejar vacío si no tiene") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = attentionDay,
                        onValueChange = { attentionDay = it },
                        label = { Text("Día Atención") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = attentionHours,
                        onValueChange = { attentionHours = it },
                        label = { Text("Horario") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            TeacherDirectoryEntry(
                                name = name.trim(),
                                roleOrGrade = role.trim().ifBlank { "Docente Titular" },
                                subject = subject.trim().ifBlank { "Materia General" },
                                email = email.trim().ifBlank { null },
                                attentionDay = attentionDay.trim().ifBlank { "Lunes a Viernes" },
                                attentionHours = attentionHours.trim().ifBlank { "12:30 PM - 1:20 PM" },
                                avatarEmoji = emoji.trim().ifBlank { "👨‍🏫" },
                                subjectsIn7th = subjectsIn7th.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar 💾")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditClassSlotDialog(
    initialDay: Int,
    initialSlot: OfficialScheduleClassSlot?,
    onDismiss: () -> Unit,
    onSave: (Int, OfficialScheduleClassSlot) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(initialDay) }
    var timeSlot by remember { mutableStateOf(initialSlot?.timeSlot ?: "7:00 - 7:50") }
    var subject by remember { mutableStateOf(initialSlot?.subject ?: "") }
    var teacher by remember { mutableStateOf(initialSlot?.teacher ?: "") }
    var classroom by remember { mutableStateOf(initialSlot?.classroom ?: "Aula 101") }
    var emoji by remember { mutableStateOf(initialSlot?.iconEmoji ?: "📚") }
    var isBreak by remember { mutableStateOf(initialSlot?.isBreak ?: false) }

    val daysOfWeek = listOf(1 to "Lunes", 2 to "Martes", 3 to "Miércoles", 4 to "Jueves", 5 to "Viernes")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSlot != null) "Editar Bloque de Clase" else "Agregar Bloque de Clase", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Day selector chips
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    daysOfWeek.forEach { (num, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedDay == num) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).clickable { selectedDay = num }
                        ) {
                            Text(
                                text = label.take(3),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = if (selectedDay == num) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Franja Horaria") },
                    placeholder = { Text("Ej: 7:00 - 7:50") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia o Actividad") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Docente") },
                    placeholder = { Text("Nombre del Docente") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isBreak, onCheckedChange = { isBreak = it })
                    Text("¿Es Descanso / Recreo? 🥪", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        onSave(
                            selectedDay,
                            OfficialScheduleClassSlot(
                                timeSlot = timeSlot.trim().ifBlank { "7:00 - 7:50" },
                                subject = subject.trim(),
                                teacher = teacher.trim().ifBlank { "Docente" },
                                classroom = "",
                                iconEmoji = emoji.trim().ifBlank { if (isBreak) "🥪" else "📚" },
                                isBreak = isBreak
                            )
                        )
                    }
                },
                enabled = subject.isNotBlank()
            ) {
                Text("Guardar 💾")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

