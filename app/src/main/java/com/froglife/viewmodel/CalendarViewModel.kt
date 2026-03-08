package com.froglife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.froglife.data.Activity
import com.froglife.data.ActivityLog
import com.froglife.data.ActivityType
import com.froglife.data.DayComment
import com.froglife.data.FrogRepository
import com.froglife.utils.DateUtils
import com.froglife.utils.StatusCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class CalendarView {
    DAY, WEEK, MONTH, YEAR
}

class CalendarViewModel(private val repository: FrogRepository) : ViewModel() {
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _calendarView = MutableStateFlow(CalendarView.DAY)
    val calendarView: StateFlow<CalendarView> = _calendarView.asStateFlow()

    private val _selectedFrogId = MutableStateFlow<Long?>(null)
    val selectedFrogId: StateFlow<Long?> = _selectedFrogId.asStateFlow()

    private val logsCache = mutableMapOf<Long, StateFlow<List<ActivityLog>>>()

    fun setSelectedDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
    }

    fun setCalendarView(view: CalendarView) {
        _calendarView.value = view
    }

    fun setSelectedFrogId(frogId: Long?) {
        _selectedFrogId.value = frogId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLogsForCurrentView(frogId: Long): StateFlow<List<ActivityLog>> {
        return logsCache.getOrPut(frogId) {
            combine(_selectedDate, _calendarView) { dateMillis, view ->
                val date = Date(dateMillis)
                val (start, end) = when (view) {
                    CalendarView.DAY -> Pair(DateUtils.getStartOfDay(date), DateUtils.getEndOfDay(date))
                    CalendarView.WEEK -> Pair(DateUtils.getStartOfWeek(date), DateUtils.getEndOfWeek(date))
                    CalendarView.MONTH -> Pair(DateUtils.getStartOfMonth(date), DateUtils.getEndOfMonth(date))
                    CalendarView.YEAR -> Pair(DateUtils.getStartOfYear(date), DateUtils.getEndOfYear(date))
                }
                Pair(start, end)
            }.flatMapLatest { (start, end) ->
                repository.getLogsForFrogInDateRange(frogId, start, end)
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    fun updateActivityLog(frogId: Long, activityId: Long, date: Long, value: String, activity: Activity, isDelete: Boolean = false) {
        viewModelScope.launch {
            val dayStart = DateUtils.getStartOfDay(Date(date))
            val dayEnd = DateUtils.getEndOfDay(Date(date))

            val existingLog = repository.getLogForFrogActivityDate(frogId, activityId, dayStart, dayEnd).first()

            if (isDelete) {
                // Delete the log
                if (existingLog != null) {
                    repository.deleteActivityLog(existingLog)
                }
            } else {
                // Calculate points
                val points = when (activity.type) {
                    ActivityType.BOOLEAN -> if (value == "true") activity.wealthAmount else 0
                    ActivityType.INTEGER -> (value.toIntOrNull() ?: 0) * activity.wealthAmount
                }

                if (existingLog != null) {
                    // Update existing log
                    repository.updateActivityLog(
                        existingLog.copy(value = value, pointsEarned = points)
                    )
                } else {
                    // Create new log
                    repository.insertActivityLog(
                        ActivityLog(
                            frogId = frogId,
                            activityId = activityId,
                            date = dayStart,
                            value = value,
                            pointsEarned = points
                        )
                    )
                }
            }

            // Recalculate total and current month points from all logs
            recalculateFrogPoints(frogId)
        }
    }

    private suspend fun recalculateFrogPoints(frogId: Long) {
        val frog = repository.getFrogById(frogId).first() ?: return

        // Calculate total points from all logs
        val allLogs = repository.getLogsForFrogInDateRange(frogId, 0, Long.MAX_VALUE).first()
        val totalPoints = allLogs.sumOf { it.pointsEarned }.coerceAtLeast(10) // Start with minimum 10

        // Calculate current month points
        val now = Date()
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth = DateUtils.getEndOfMonth(now)
        val monthLogs = repository.getLogsForFrogInDateRange(frogId, startOfMonth, endOfMonth).first()
        val monthPoints = monthLogs.sumOf { it.pointsEarned }

        // Get settings and calculate status
        val settings = repository.getSettings().first()
        val newStatus = if (settings != null) {
            StatusCalculator.calculateStatus(totalPoints, settings)
        } else {
            frog.status
        }

        // Update frog with recalculated values
        repository.updateFrog(
            frog.copy(
                wealthPoints = totalPoints,
                currentMonthPoints = monthPoints,
                status = newStatus
            )
        )
    }

    // Day Comments
    fun getCommentForDay(frogId: Long, date: Long): Flow<DayComment?> =
        repository.getCommentForDay(frogId, date)

    fun getCommentsInRange(frogId: Long, startDate: Long, endDate: Long): Flow<List<DayComment>> =
        repository.getCommentsInRange(frogId, startDate, endDate)

    fun saveComment(frogId: Long, date: Long, commentText: String) {
        viewModelScope.launch {
            val dayStart = DateUtils.getStartOfDay(Date(date))
            val existing = repository.getCommentForDay(frogId, dayStart).first()

            if (existing != null) {
                repository.updateDayComment(existing.copy(comment = commentText))
            } else {
                repository.insertDayComment(
                    DayComment(frogId = frogId, date = dayStart, comment = commentText)
                )
            }
        }
    }

    fun deleteComment(frogId: Long, date: Long) {
        viewModelScope.launch {
            val dayStart = DateUtils.getStartOfDay(Date(date))
            val existing = repository.getCommentForDay(frogId, dayStart).first()
            existing?.let { repository.deleteDayComment(it) }
        }
    }
}
