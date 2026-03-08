package com.froglife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.froglife.data.Reward
import com.froglife.data.RewardRedemption
import com.froglife.data.FrogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RewardViewModel(private val repository: FrogRepository) : ViewModel() {
    val allRewards: StateFlow<List<Reward>> = repository.getAllRewards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRedemptions: StateFlow<List<RewardRedemption>> = repository.getAllRedemptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val redemptionsCache = mutableMapOf<Long, StateFlow<List<RewardRedemption>>>()

    fun getRedemptionsForFrog(frogId: Long): StateFlow<List<RewardRedemption>> {
        return redemptionsCache.getOrPut(frogId) {
            repository.getRedemptionsForFrog(frogId)
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    fun insertReward(reward: Reward) {
        viewModelScope.launch {
            repository.insertReward(reward)
        }
    }

    fun updateReward(reward: Reward) {
        viewModelScope.launch {
            repository.updateReward(reward)
        }
    }

    fun deleteReward(reward: Reward) {
        viewModelScope.launch {
            repository.deleteReward(reward)
        }
    }

    fun insertRedemption(redemption: RewardRedemption) {
        viewModelScope.launch {
            repository.insertRedemption(redemption)
        }
    }

    fun updateRedemption(redemption: RewardRedemption) {
        viewModelScope.launch {
            repository.updateRedemption(redemption)
        }
    }

    fun deleteRedemption(redemption: RewardRedemption) {
        viewModelScope.launch {
            repository.deleteRedemption(redemption)
        }
    }

    // Suspend versions for import
    suspend fun insertRewardSync(reward: Reward) = repository.insertReward(reward)
    suspend fun insertRedemptionSync(redemption: RewardRedemption) = repository.insertRedemption(redemption)

    // Get fresh data (bypasses StateFlow cache)
    fun getAllRewards() = repository.getAllRewards()
    fun getAllRedemptions() = repository.getAllRedemptions()
}
