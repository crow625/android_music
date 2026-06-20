package com.example.androidmusic.domain.model

import java.time.Instant
import java.time.LocalDate

/** A single recorded listening event (≥ 5s of play). */
data class PlayEvent(
    val trackId: String,
    val trackTitle: String,
    val artistName: String,
    val albumName: String,
    val startedAt: Instant,
    val durationListenedMs: Long,
)

/** A period to aggregate stats over. */
sealed interface StatPeriod {
    data class Month(val year: Int, val month: Int) : StatPeriod
    data class Year(val year: Int) : StatPeriod
    data object AllTime : StatPeriod
}

data class StatsSummary(
    val topTracks: List<RankedItem>,
    val topArtists: List<RankedItem>,
    val topAlbums: List<RankedItem>,
    val totalListeningMs: Long,
)

data class RankedItem(val name: String, val totalMs: Long, val playCount: Int)

data class DailyListening(val date: LocalDate, val totalMs: Long)
