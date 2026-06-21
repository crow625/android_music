package com.example.androidmusic.ui.player

data class NowPlayingTrack(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUri: String?,
)

data class PlayerUiState(
    val isConnected: Boolean = false,
    val currentTrack: NowPlayingTrack? = null,
    val isPlaying: Boolean = false,
) {
    val hasTrack: Boolean get() = currentTrack != null
}
