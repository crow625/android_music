package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.player.MiniPlayer
import com.example.androidmusic.ui.player.NowPlayingTrack
import com.example.androidmusic.ui.player.PlayerUiState
import com.example.androidmusic.ui.theme.AndroidMusicTheme

private val sampleTrack = NowPlayingTrack(
    id = "1",
    title = "Chop Suey!",
    artist = "System of a Down",
    album = "Toxicity",
    albumArtUri = null,
)

@Preview(name = "MiniPlayer – playing", showBackground = true)
@Composable
fun MiniPlayerPlayingPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        MiniPlayer(
            state = PlayerUiState(isConnected = true, currentTrack = sampleTrack, isPlaying = true),
            onPlayPause = {},
            onNext = {},
            onClick = {},
        )
    }
}

@Preview(name = "MiniPlayer – paused", showBackground = true)
@Composable
fun MiniPlayerPausedPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        MiniPlayer(
            state = PlayerUiState(isConnected = true, currentTrack = sampleTrack, isPlaying = false),
            onPlayPause = {},
            onNext = {},
            onClick = {},
        )
    }
}
