package com.froglife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.froglife.data.Activity
import com.froglife.data.FrogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityViewModel(private val repository: FrogRepository) : ViewModel() {
    val allActivities: StateFlow<List<Activity>> = repository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val frogActivitiesCache = mutableMapOf<Long, StateFlow<List<Activity>>>()

    fun getActivitiesForFrog(frogId: Long): StateFlow<List<Activity>> {
        return frogActivitiesCache.getOrPut(frogId) {
            repository.getActivitiesForFrog(frogId)
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    fun insertActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    fun attachActivityToFrog(frogId: Long, activityId: Long) {
        viewModelScope.launch {
            repository.attachActivityToFrog(frogId, activityId)
        }
    }

    fun detachActivityFromFrog(frogId: Long, activityId: Long) {
        viewModelScope.launch {
            repository.detachActivityFromFrog(frogId, activityId)
        }
    }

    // Suspend version for import
    suspend fun insertActivitySync(activity: Activity) = repository.insertActivity(activity)

    // Get fresh activities data (bypasses StateFlow cache)
    fun getAllActivities() = repository.getAllActivities()

    // Get fresh activities for a specific frog (bypasses StateFlow cache)
    fun getActivitiesForFrogDirect(frogId: Long) = repository.getActivitiesForFrog(frogId)
}
