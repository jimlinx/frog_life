package com.froglife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.navigation.NavController
import com.froglife.data.ActivityLog
import com.froglife.data.Frog
import com.froglife.data.SpecialDate
import com.froglife.viewmodel.ActivityViewModel
import com.froglife.viewmodel.CalendarViewModel
import com.froglife.viewmodel.CalendarView
import com.froglife.viewmodel.FrogViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel,
    calendarViewModel: CalendarViewModel
) {
    val frogs by frogViewModel.allFrogs.collectAsState()
    val leaderFrog by frogViewModel.leaderFrog.collectAsState()
    val selectedFrogId by calendarViewModel.selectedFrogId.collectAsState()
    val calendarView by calendarViewModel.calendarView.collectAsState()
    val selectedDate by calendarViewModel.selectedDate.collectAsState()

    val currentFrog = frogs.find { it.id == selectedFrogId } ?: frogs.firstOrNull()

    LaunchedEffect(currentFrog) {
        if (currentFrog != null && selectedFrogId == null) {
            calendarViewModel.setSelectedFrogId(currentFrog.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Frog selection row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(frogs) { frog ->
                    FrogProfileCard(
                        frog = frog,
                        isLeader = frog.id == leaderFrog?.id,
                        isSelected = frog.id == selectedFrogId,
                        onClick = { calendarViewModel.setSelectedFrogId(frog.id) }
                    )
                }
            }

            // Calendar view selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = calendarView == CalendarView.DAY,
                    onClick = { calendarViewModel.setCalendarView(CalendarView.DAY) },
                    label = { Text("Day") }
                )
                FilterChip(
                    selected = calendarView == CalendarView.WEEK,
                    onClick = { calendarViewModel.setCalendarView(CalendarView.WEEK) },
                    label = { Text("Week") }
                )
                FilterChip(
                    selected = calendarView == CalendarView.MONTH,
                    onClick = { calendarViewModel.setCalendarView(CalendarView.MONTH) },
                    label = { Text("Month") }
                )
                FilterChip(
                    selected = calendarView == CalendarView.YEAR,
                    onClick = { calendarViewModel.setCalendarView(CalendarView.YEAR) },
                    label = { Text("Year") }
                )
            }

            if (currentFrog != null) {
                val logs by calendarViewModel.getLogsForCurrentView(currentFrog.id).collectAsState()
                val activities by activityViewModel.getActivitiesForFrog(currentFrog.id).collectAsState()
                val specialDates by frogViewModel.getSpecialDatesForFrog(currentFrog.id).collectAsState(initial = emptyList())

                // Get comments for current view range
                val (commentsStartDate, commentsEndDate) = remember(selectedDate, calendarView) {
                    val date = Date(selectedDate)
                    when (calendarView) {
                        CalendarView.DAY -> Pair(com.froglife.utils.DateUtils.getStartOfDay(date), com.froglife.utils.DateUtils.getEndOfDay(date))
                        CalendarView.WEEK -> Pair(com.froglife.utils.DateUtils.getStartOfWeek(date), com.froglife.utils.DateUtils.getEndOfWeek(date))
                        CalendarView.MONTH -> Pair(com.froglife.utils.DateUtils.getStartOfMonth(date), com.froglife.utils.DateUtils.getEndOfMonth(date))
                        CalendarView.YEAR -> Pair(com.froglife.utils.DateUtils.getStartOfYear(date), com.froglife.utils.DateUtils.getEndOfYear(date))
                    }
                }
                val comments by calendarViewModel.getCommentsInRange(currentFrog.id, commentsStartDate, commentsEndDate).collectAsState(initial = emptyList())

                when (calendarView) {
                    CalendarView.YEAR -> YearView(
                        selectedDate = selectedDate,
                        logs = logs,
                        activities = activities,
                        specialDates = specialDates,
                        comments = comments,
                        onMonthClick = { month ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedDate
                                set(Calendar.MONTH, month)
                            }
                            calendarViewModel.setSelectedDate(cal.timeInMillis)
                            calendarViewModel.setCalendarView(CalendarView.MONTH)
                        },
                        onYearChange = { delta ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedDate
                                add(Calendar.YEAR, delta)
                            }
                            calendarViewModel.setSelectedDate(cal.timeInMillis)
                        }
                    )
                    CalendarView.MONTH -> MonthView(
                        selectedDate = selectedDate,
                        logs = logs,
                        activities = activities,
                        specialDates = specialDates,
                        comments = comments,
                        onDayClick = { dayMillis ->
                            calendarViewModel.setSelectedDate(dayMillis)
                            calendarViewModel.setCalendarView(CalendarView.DAY)
                        },
                        onMonthChange = { delta ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedDate
                                add(Calendar.MONTH, delta)
                            }
                            calendarViewModel.setSelectedDate(cal.timeInMillis)
                        }
                    )
                    CalendarView.WEEK -> WeekView(
                        selectedDate = selectedDate,
                        logs = logs,
                        activities = activities,
                        comments = comments,
                        calendarViewModel = calendarViewModel,
                        onDayClick = { dayMillis ->
                            calendarViewModel.setSelectedDate(dayMillis)
                            calendarViewModel.setCalendarView(CalendarView.DAY)
                        },
                        onWeekChange = { delta ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedDate
                                add(Calendar.WEEK_OF_YEAR, delta)
                            }
                            calendarViewModel.setSelectedDate(cal.timeInMillis)
                        }
                    )
                    CalendarView.DAY -> DayView(
                        selectedDate = selectedDate,
                        logs = logs,
                        activities = activities,
                        specialDates = specialDates,
                        frogId = currentFrog.id,
                        calendarViewModel = calendarViewModel,
                        onDayChange = { delta ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedDate
                                add(Calendar.DAY_OF_MONTH, delta)
                            }
                            calendarViewModel.setSelectedDate(cal.timeInMillis)
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No frogs available. Add a frog first!")
                }
            }
        }
    }
}

@Composable
fun YearView(
    selectedDate: Long,
    logs: List<ActivityLog>,
    activities: List<com.froglife.data.Activity>,
    specialDates: List<com.froglife.data.SpecialDate>,
    comments: List<com.froglife.data.DayComment>,
    onMonthClick: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val year = cal.get(Calendar.YEAR)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onYearChange(-1) }) {
                Icon(Icons.Default.ChevronLeft, "Previous Year")
            }
            Text(year.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onYearChange(1) }) {
                Icon(Icons.Default.ChevronRight, "Next Year")
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(12) { month ->
                val monthLogs = logs.filter {
                    val logCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    logCal.get(Calendar.MONTH) == month && logCal.get(Calendar.YEAR) == year
                }
                val totalPoints = monthLogs.sumOf { it.pointsEarned }

                val monthSpecialDates = specialDates.filter {
                    val specialCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    specialCal.get(Calendar.MONTH) == month && specialCal.get(Calendar.YEAR) == year
                }

                val monthComments = comments.filter {
                    val commentCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    commentCal.get(Calendar.MONTH) == month && commentCal.get(Calendar.YEAR) == year
                }

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onMonthClick(month) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            SimpleDateFormat("MMM", Locale.getDefault()).format(
                                Calendar.getInstance().apply {
                                    set(Calendar.MONTH, month)
                                }.time
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${monthLogs.size} activities",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                        if (monthSpecialDates.isNotEmpty()) {
                            Text(
                                "${monthSpecialDates.size} special dates",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (monthComments.isNotEmpty()) {
                            Text(
                                "${monthComments.size} notes",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (totalPoints != 0) {
                            Text(
                                "${if (totalPoints > 0) "+" else ""}$totalPoints pts",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = if (totalPoints > 0) Color.Green else Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthView(
    selectedDate: Long,
    logs: List<ActivityLog>,
    activities: List<com.froglife.data.Activity>,
    specialDates: List<com.froglife.data.SpecialDate>,
    comments: List<com.froglife.data.DayComment>,
    onDayClick: (Long) -> Unit,
    onMonthChange: (Int) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(-1) }) {
                Icon(Icons.Default.ChevronLeft, "Previous Month")
            }
            Text(
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(1) }) {
                Icon(Icons.Default.ChevronRight, "Next Month")
            }
        }

        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Calendar grid
        val firstDayOfMonth = Calendar.getInstance().apply {
            set(year, month, 1)
        }
        val daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(4.dp)
        ) {
            // Empty cells before first day
            items(firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            // Days of month
            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val dayCal = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val dayMillis = dayCal.timeInMillis

                val dayLogs = logs.filter {
                    val logCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    logCal.get(Calendar.YEAR) == year &&
                            logCal.get(Calendar.MONTH) == month &&
                            logCal.get(Calendar.DAY_OF_MONTH) == day
                }

                val totalPoints = dayLogs.sumOf { it.pointsEarned }
                val activityColors = dayLogs.mapNotNull { log ->
                    activities.find { it.id == log.activityId }?.color
                }.distinct()

                // Check if this day has a special date
                val hasSpecialDate = specialDates.any { specialDate ->
                    val specialCal = Calendar.getInstance().apply { timeInMillis = specialDate.date }
                    specialCal.get(Calendar.YEAR) == year &&
                            specialCal.get(Calendar.MONTH) == month &&
                            specialCal.get(Calendar.DAY_OF_MONTH) == day
                }

                // Check if this day has a comment
                val hasComment = comments.any { comment ->
                    val commentCal = Calendar.getInstance().apply { timeInMillis = comment.date }
                    commentCal.get(Calendar.YEAR) == year &&
                            commentCal.get(Calendar.MONTH) == month &&
                            commentCal.get(Calendar.DAY_OF_MONTH) == day
                }

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clickable { onDayClick(dayMillis) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (dayLogs.isNotEmpty())
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(day.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            if (hasSpecialDate) {
                                Text("⭐", fontSize = 8.sp)
                            }
                            if (hasComment) {
                                Text("📝", fontSize = 8.sp)
                            }
                        }
                        if (dayLogs.isNotEmpty()) {
                            Text(
                                "${dayLogs.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                activityColors.take(3).forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(Color(color.toULong()))
                                    )
                                }
                            }
                            if (totalPoints != 0) {
                                Text(
                                    "${if (totalPoints > 0) "+" else ""}$totalPoints",
                                    fontSize = 8.sp,
                                    color = if (totalPoints > 0) Color.Green else Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekView(
    selectedDate: Long,
    logs: List<ActivityLog>,
    activities: List<com.froglife.data.Activity>,
    comments: List<com.froglife.data.DayComment>,
    calendarViewModel: CalendarViewModel,
    onDayClick: (Long) -> Unit,
    onWeekChange: (Int) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selectedDate
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onWeekChange(-1) }) {
                    Icon(Icons.Default.ChevronLeft, "Previous Week")
                }
                Button(
                    onClick = {
                        val today = System.currentTimeMillis()
                        calendarViewModel.setSelectedDate(today)
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Today", fontSize = 12.sp)
                }
            }
            Text(
                "Week of ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(cal.time)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onWeekChange(1) }) {
                Icon(Icons.Default.ChevronRight, "Next Week")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(7) { dayOffset ->
                val dayCal = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    add(Calendar.DAY_OF_MONTH, dayOffset)
                }
                val dayMillis = dayCal.timeInMillis

                val dayLogs = logs.filter {
                    val logCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    logCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                            logCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                }

                val hasComment = comments.any { comment ->
                    val commentCal = Calendar.getInstance().apply { timeInMillis = comment.date }
                    commentCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                            commentCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDayClick(dayMillis) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(dayCal.time),
                                fontWeight = FontWeight.Bold
                            )
                            if (hasComment) {
                                Text("📝", fontSize = 12.sp)
                            }
                        }
                        if (dayLogs.isEmpty()) {
                            Text("No activities", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        } else {
                            dayLogs.forEach { log ->
                                val activity = activities.find { it.id == log.activityId }
                                activity?.let {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(Color(it.color.toULong()))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline)
                                            )
                                            Text(it.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Text(
                                            "${if (log.pointsEarned > 0) "+" else ""}${log.pointsEarned}",
                                            fontSize = 14.sp,
                                            color = if (log.pointsEarned > 0) Color.Green else Color.Red,
                                            fontWeight = FontWeight.Bold
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

@Composable
fun DayView(
    selectedDate: Long,
    logs: List<ActivityLog>,
    activities: List<com.froglife.data.Activity>,
    specialDates: List<com.froglife.data.SpecialDate>,
    frogId: Long,
    calendarViewModel: CalendarViewModel,
    onDayChange: (Int) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<ActivityLog?>(null) }
    var showCommentDialog by remember { mutableStateOf(false) }

    val dayStart = com.froglife.utils.DateUtils.getStartOfDay(cal.time)
    val dayComment by calendarViewModel.getCommentForDay(frogId, dayStart).collectAsState(initial = null)

    val dayLogs = logs.filter {
        val logCal = Calendar.getInstance().apply { timeInMillis = it.date }
        logCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                logCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }

    // Check for special dates on this day
    val daySpecialDates = specialDates.filter { specialDate ->
        val specialCal = Calendar.getInstance().apply { timeInMillis = specialDate.date }
        specialCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                specialCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }

    val totalPoints = dayLogs.sumOf { it.pointsEarned }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onDayChange(-1) }) {
                    Icon(Icons.Default.ChevronLeft, "Previous Day")
                }
                Button(
                    onClick = {
                        val today = System.currentTimeMillis()
                        calendarViewModel.setSelectedDate(today)
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Today", fontSize = 12.sp)
                }
            }
            Text(
                SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(cal.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onDayChange(1) }) {
                Icon(Icons.Default.ChevronRight, "Next Day")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Activities: ${dayLogs.size}", fontWeight = FontWeight.Bold)
                    Text(
                        "Total Points: ${if (totalPoints > 0) "+" else ""}$totalPoints",
                        fontWeight = FontWeight.Bold,
                        color = if (totalPoints > 0) Color.Green else if (totalPoints < 0) Color.Red else Color.Gray
                    )
                }
                Button(
                    onClick = {
                        showAddDialog = true
                    }
                ) {
                    Text("+ Track Activity")
                }
            }
        }

        // Special Dates on this day
        if (daySpecialDates.isNotEmpty()) {
            daySpecialDates.forEach { specialDate ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐", fontSize = 24.sp)
                        Text(
                            specialDate.description,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (dayLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No activities logged for this day", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (activities.isNotEmpty()) {
                        Text("Tap 'Track Activity' to add one", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Text("Add activities in 'Manage Activities' first", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayLogs, key = { it.id }) { log ->
                    val activity = activities.find { it.id == log.activityId }
                    activity?.let {
                        ActivityLogCard(
                            log = log,
                            activity = it,
                            onEdit = { editingLog = log },
                            onDelete = {
                                calendarViewModel.updateActivityLog(
                                    frogId = frogId,
                                    activityId = log.activityId,
                                    date = selectedDate,
                                    value = "",
                                    activity = it,
                                    isDelete = true
                                )
                            }
                        )
                    }
                }
            }
        }

        // Day Comment Section (always visible)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Day Notes", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { showCommentDialog = true }) {
                            Text(if (dayComment == null) "+ Add Note" else "Edit")
                        }
                        if (dayComment != null) {
                            TextButton(
                                onClick = { calendarViewModel.deleteComment(frogId, selectedDate) }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                if (dayComment != null) {
                    Text(
                        dayComment!!.comment,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        "No notes for this day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Add/Edit Activity Dialog
    if (showAddDialog || editingLog != null) {
        ActivityLogDialog(
            activities = activities,
            existingLog = editingLog,
            selectedDate = selectedDate,
            onDismiss = {
                showAddDialog = false
                editingLog = null
            },
            onSave = { activityId, value ->
                val activity = activities.find { it.id == activityId }
                if (activity != null) {
                    calendarViewModel.updateActivityLog(
                        frogId = frogId,
                        activityId = activityId,
                        date = selectedDate,
                        value = value,
                        activity = activity,
                        isDelete = false
                    )
                }
                showAddDialog = false
                editingLog = null
            }
        )
    }

    // Comment Dialog
    if (showCommentDialog) {
        var commentText by remember { mutableStateOf(dayComment?.comment ?: "") }
        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text(if (dayComment == null) "Add Note" else "Edit Note") },
            text = {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            calendarViewModel.saveComment(frogId, selectedDate, commentText)
                        }
                        showCommentDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActivityLogCard(
    log: ActivityLog,
    activity: com.froglife.data.Activity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Safely convert color with fallback
    val backgroundColor = try {
        Color(activity.color.toULong()).copy(alpha = 0.3f)
    } catch (e: Exception) {
        Color(0xFF9C27B0).copy(alpha = 0.3f) // Fallback purple
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(activity.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (activity.description.isNotEmpty()) {
                        Text(activity.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text("Value: ${log.value}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Logged: ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.date))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${if (log.pointsEarned > 0) "+" else ""}${log.pointsEarned} pts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (log.pointsEarned > 0) Color.Green else Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Text("✏️", fontSize = 16.sp)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                            Text("🗑️", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Activity Log") },
            text = { Text("Are you sure you want to delete this activity log for '${activity.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActivityLogDialog(
    activities: List<com.froglife.data.Activity>,
    existingLog: ActivityLog?,
    selectedDate: Long,
    onDismiss: () -> Unit,
    onSave: (activityId: Long, value: String) -> Unit
) {
    var selectedActivityId by remember { mutableStateOf(existingLog?.activityId ?: activities.firstOrNull()?.id ?: 0L) }
    val selectedActivity = activities.find { it.id == selectedActivityId }
    var value by remember(selectedActivityId) {
        mutableStateOf(
            existingLog?.value ?: when (selectedActivity?.type) {
                com.froglife.data.ActivityType.BOOLEAN -> "true"
                com.froglife.data.ActivityType.INTEGER -> ""
                else -> ""
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingLog != null) "Edit Activity" else "Track Activity", fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(selectedDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                if (activities.isEmpty()) {
                    Text(
                        "No activities attached to this frog.\n\nGo to Manage Frogs → Edit frog → Attach activities first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                } else {
                    Text("Select Activity:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Scrollable activity list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(activities) { activity ->
                            // Safely convert color with fallback
                            val activityColor = try {
                                Color(activity.color.toULong())
                            } catch (e: Exception) {
                                Color(0xFF9C27B0) // Fallback purple
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedActivityId = activity.id }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedActivityId == activity.id,
                                    onClick = { selectedActivityId = activity.id },
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(activityColor)
                                )
                                Text(activity.name, fontSize = 13.sp)
                            }
                        }
                    }

                    selectedActivity?.let { activity ->
                        Spacer(modifier = Modifier.height(4.dp))
                        when (activity.type) {
                            com.froglife.data.ActivityType.BOOLEAN -> {
                                Text(
                                    "✓ Will be marked as completed",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            com.froglife.data.ActivityType.INTEGER -> {
                                Text("Value:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { value = it },
                                    label = { Text("Enter value", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )
                            }
                        }

                        Text(
                            "Points: ${if (activity.wealthAmount > 0) "+" else ""}${calculatePoints(activity, value)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (activity.wealthAmount > 0) Color.Green else Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(selectedActivityId, value) },
                enabled = activities.isNotEmpty() && selectedActivity != null && value.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun calculatePoints(activity: com.froglife.data.Activity, value: String): Int {
    return when (activity.type) {
        com.froglife.data.ActivityType.BOOLEAN -> if (value == "true") activity.wealthAmount else 0
        com.froglife.data.ActivityType.INTEGER -> (value.toIntOrNull() ?: 0) * activity.wealthAmount
    }
}

@Composable
fun FrogProfileCard(frog: Frog, isLeader: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val statusColor = when (frog.status.name) {
        "ROCK" -> Color.Gray
        "COPPER" -> Color(0xFFB87333)
        "BRONZE" -> Color(0xFFCD7F32)
        "SILVER" -> Color(0xFFC0C0C0)
        "GOLD" -> Color(0xFFFFD700)
        "DIAMOND" -> Color(0xFFB9F2FF)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopCenter) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .border(3.dp, statusColor, MaterialTheme.shapes.medium)
                        .background(Color.White, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getFrogIcon(frog.presetIconId), fontSize = 30.sp)
                }
                if (isLeader) {
                    Text("👑", fontSize = 20.sp, modifier = Modifier.offset(y = (-10).dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                frog.name,
                fontSize = 12.sp,
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
