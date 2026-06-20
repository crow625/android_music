package com.example.androidmusic.domain.repository

import com.example.androidmusic.domain.model.DailyListening
import com.example.androidmusic.domain.model.PlayEvent
import com.example.androidmusic.domain.model.StatPeriod
import com.example.androidmusic.domain.model.StatsSummary
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    suspend fun recordPlayEvent(event: PlayEvent)
    suspend fun querySummary(period: StatPeriod): StatsSummary
    fun observeHistory(): Flow<List<DailyListening>>
}
