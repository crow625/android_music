package com.example.androidmusic.ui.player

import com.example.androidmusic.domain.model.RepeatMode

data class NowPlayingTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUri: String?,
)

data class QueueItemUi(
    val index: Int,
    val id: String,
    val title: String,
    val artist: String,
    val isCurrent: Boolean,
)

data class PlayerUiState(
    val isConnected: Boolean = false,
    val currentTrack: NowPlayingTrack? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val isShuffleOn: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val queue: List<QueueItemUi> = emptyList(),
) {
    val hasTrack: Boolean get() = currentTrack != null
}
