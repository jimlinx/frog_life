package com.froglife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.froglife.data.*
import com.froglife.utils.StatusCalculator
import com.froglife.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

class FrogViewModel(private val repository: FrogRepository) : ViewModel() {
    val allFrogs: StateFlow<List<Frog>> = repository.getAllFrogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderFrog: StateFlow<Frog?> = repository.getLeaderFrog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val settings: StateFlow<AppSettings?> = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedFrog = MutableStateFlow<Frog?>(null)
    val selectedFrog: StateFlow<Frog?> = _selectedFrog.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // Initialize settings if not exists - wait for actual database value
                val currentSettings = repository.getSettings().first()
                if (currentSettings == null) {
                    repository.insertSettings(
                        AppSettings(
                            id = 1,
                            rockThreshold = 10,
                            copperThreshold = 50,
                            bronzeThreshold = 100,
                            silverThreshold = 200,
                            goldThreshold = 400,
                            diamondThreshold = 800
                        )
                    )
                }

                // Wait a bit for frogs to load, then recalculate
                kotlinx.coroutines.delay(500)

                // Check if we need to process monthly wins for a new month
                // This must happen BEFORE updateCurrentMonthPoints() so we capture previous month's points
                checkAndProcessMonthlyWins()

                // Recalculate current month points for all frogs on app start
                updateCurrentMonthPoints()
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }

    fun selectFrog(frog: Frog?) {
        _selectedFrog.value = frog
    }

    fun insertFrog(frog: Frog) {
        viewModelScope.launch {
            repository.insertFrog(frog)
        }
    }

    fun updateFrog(frog: Frog) {
        viewModelScope.launch {
            // Recalculate everything from activity logs to ensure accuracy
            val now = Date()
            val startOfMonth = DateUtils.getStartOfMonth(now)
            val endOfMonth = DateUtils.getEndOfMonth(now)

            // Calculate current month points
            val monthLogs = repository.getLogsForFrogInDateRange(frog.id, startOfMonth, endOfMonth).first()
            val monthPoints = monthLogs.sumOf { it.pointsEarned }

            // Calculate total wealth points
            val allLogs = repository.getLogsForFrogInDateRange(frog.id, 0, Long.MAX_VALUE).first()
            val totalPoints = allLogs.sumOf { it.pointsEarned }.coerceAtLeast(10)

            // Recalculate status based on total wealth points
            val currentSettings = settings.value ?: return@launch
            val updatedStatus = StatusCalculator.calculateStatus(totalPoints, currentSettings)

            repository.updateFrog(
                frog.copy(
                    wealthPoints = totalPoints,
                    currentMonthPoints = monthPoints,
                    status = updatedStatus
                )
            )
        }
    }

    fun deleteFrog(frog: Frog) {
        viewModelScope.launch {
            repository.deleteFrog(frog)
        }
    }

    fun adjustWealthPoints(frogId: Long, adjustment: Int) {
        viewModelScope.launch {
            val frog = repository.getFrogById(frogId).first() ?: return@launch
            val newPoints = (frog.wealthPoints + adjustment).coerceAtLeast(0)
            val currentSettings = settings.value ?: return@launch
            val newStatus = StatusCalculator.calculateStatus(newPoints, currentSettings)
            repository.updateFrog(frog.copy(wealthPoints = newPoints, status = newStatus))
        }
    }

    fun insertSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.insertSettings(settings)
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            // Recalculate all frog statuses
            allFrogs.value.forEach { frog ->
                val newStatus = StatusCalculator.calculateStatus(frog.wealthPoints, settings)
                repository.updateFrog(frog.copy(status = newStatus))
            }
        }
    }

    // Special Dates
    fun getSpecialDatesForFrog(frogId: Long): Flow<List<SpecialDate>> =
        repository.getSpecialDatesForFrog(frogId)

    fun getSpecialDatesInRange(startDate: Long, endDate: Long): Flow<List<SpecialDate>> =
        repository.getSpecialDatesInRange(startDate, endDate)

    fun insertSpecialDate(specialDate: SpecialDate) {
        viewModelScope.launch {
            repository.insertSpecialDate(specialDate)
        }
    }

    fun updateSpecialDate(specialDate: SpecialDate) {
        viewModelScope.launch {
            repository.updateSpecialDate(specialDate)
        }
    }

    fun deleteSpecialDate(specialDate: SpecialDate) {
        viewModelScope.launch {
            repository.deleteSpecialDate(specialDate)
        }
    }

    fun deleteActivityLog(log: ActivityLog) {
        viewModelScope.launch {
            repository.deleteActivityLog(log)
        }
    }

    fun deleteDayComment(comment: DayComment) {
        viewModelScope.launch {
            repository.deleteDayComment(comment)
        }
    }

    // Export/Import methods
    fun getAllCrossRefs(): Flow<List<FrogActivityCrossRef>> = repository.getAllCrossRefs()
    fun getAllActivityLogs(): Flow<List<ActivityLog>> = repository.getAllActivityLogs()
    fun getAllSpecialDates(): Flow<List<SpecialDate>> = repository.getAllSpecialDates()
    fun getAllDayComments(): Flow<List<DayComment>> = repository.getAllDayComments()

    fun insertCrossRef(crossRef: FrogActivityCrossRef) {
        viewModelScope.launch {
            repository.attachActivityToFrog(crossRef.frogId, crossRef.activityId)
        }
    }

    fun insertActivityLog(log: ActivityLog) {
        viewModelScope.launch {
            repository.insertActivityLog(log)
        }
    }

    fun insertDayComment(comment: DayComment) {
        viewModelScope.launch {
            repository.insertDayComment(comment)
        }
    }

    fun updateDayComment(comment: DayComment) {
        viewModelScope.launch {
            repository.updateDayComment(comment)
        }
    }

    // Suspend versions for import (to ensure operations complete before proceeding)
    suspend fun insertFrogSync(frog: Frog) = repository.insertFrog(frog)
    suspend fun insertActivitySync(activity: Activity) = repository.insertActivity(activity)
    suspend fun insertCrossRefSync(crossRef: FrogActivityCrossRef) = repository.attachActivityToFrog(crossRef.frogId, crossRef.activityId)
    suspend fun insertActivityLogSync(log: ActivityLog) = repository.insertActivityLog(log)
    suspend fun insertSpecialDateSync(specialDate: SpecialDate) = repository.insertSpecialDate(specialDate)
    suspend fun insertDayCommentSync(comment: DayComment) = repository.insertDayComment(comment)
    suspend fun insertSettingsSync(settings: AppSettings) = repository.insertSettings(settings)
    suspend fun clearAllData() = repository.clearAllData()

    // Current month points calculation
    suspend fun updateCurrentMonthPoints() {
        val now = Date()
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth = DateUtils.getEndOfMonth(now)

        // Wait for settings to be loaded
        val currentSettings = settings.first() ?: return

        // Get fresh frog list
        val frogList = repository.getAllFrogs().first()

        frogList.forEach { frog ->
            val logs = repository.getLogsForFrogInDateRange(frog.id, startOfMonth, endOfMonth).first()
            val monthPoints = logs.sumOf { it.pointsEarned }

            // Also recalculate total wealth points to ensure consistency
            val allLogs = repository.getLogsForFrogInDateRange(frog.id, 0, Long.MAX_VALUE).first()
            val totalPoints = allLogs.sumOf { it.pointsEarned }.coerceAtLeast(10) // Start with minimum 10

            val newStatus = StatusCalculator.calculateStatus(totalPoints, currentSettings)

            repository.updateFrog(
                frog.copy(
                    currentMonthPoints = monthPoints,
                    wealthPoints = totalPoints,
                    status = newStatus
                )
            )
        }
    }

    // Force recalculation of all frog stats (can be called manually)
    fun recalculateAllFrogStats() {
        viewModelScope.launch {
            updateCurrentMonthPoints()
        }
    }

    // Check if we need to process monthly wins for a new month
    private suspend fun checkAndProcessMonthlyWins() {
        val currentSettings = settings.first() ?: return
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

        // Only process if we haven't processed this month yet
        if (currentSettings.lastMonthlyWinsProcessed != currentMonth) {
            processMonthlyWins()
        }
    }

    // Process monthly wins - can be called manually or automatically
    suspend fun processMonthlyWins() {
        val frogs = allFrogs.value
        if (frogs.isEmpty()) return

        // Find the winner (highest current month points)
        val winner = frogs.maxByOrNull { it.currentMonthPoints }

        winner?.let { winningFrog ->
            // Only record if frog has earned points
            if (winningFrog.currentMonthPoints > 0) {
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

                // Only increment if we haven't already recorded this month for this frog
                if (winningFrog.lastMonthWinRecorded != currentMonth) {
                    repository.updateFrog(
                        winningFrog.copy(
                            monthlyWins = winningFrog.monthlyWins + 1,
                            lastMonthWinRecorded = currentMonth
                        )
                    )
                }

                // Update settings to mark this month as processed
                val currentSettings = settings.first()
                currentSettings?.let {
                    repository.updateSettings(
                        it.copy(lastMonthlyWinsProcessed = currentMonth)
                    )
                }
            }
        }
    }

    // Manual monthly wins processing (for Settings button)
    fun manuallyProcessMonthlyWins() {
        viewModelScope.launch {
            processMonthlyWins()
        }
    }

    // Legacy function kept for compatibility (now calls processMonthlyWins)
    suspend fun updateMonthlyWins() {
        processMonthlyWins()
    }

    // Get fresh frogs data (bypasses StateFlow cache)
    fun getAllFrogs() = repository.getAllFrogs()

    // Get frog rankings by current month points
    fun getFrogsByCurrentMonth(): StateFlow<List<Frog>> =
        allFrogs.map { frogs ->
            frogs.sortedByDescending { it.currentMonthPoints }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculate reward balance for a frog (wealth points - total redemptions)
    suspend fun calculateRewardBalance(frogId: Long): Int {
        val frog = repository.getFrogById(frogId).first() ?: return 0
        val redemptions = repository.getRedemptionsForFrog(frogId).first()
        val totalRedemptions = redemptions.sumOf { it.pointsUsed }
        return frog.wealthPoints - totalRedemptions
    }

    // Get reward balance as a flow
    fun getRewardBalanceFlow(frogId: Long): Flow<Int> = flow {
        repository.getFrogById(frogId).combine(
            repository.getRedemptionsForFrog(frogId)
        ) { frog, redemptions ->
            val totalRedemptions = redemptions.sumOf { it.pointsUsed }
            (frog?.wealthPoints ?: 0) - totalRedemptions
        }.collect { emit(it) }
    }
}
