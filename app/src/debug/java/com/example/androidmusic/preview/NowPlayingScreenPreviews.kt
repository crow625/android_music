package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.domain.model.RepeatMode
import com.example.androidmusic.ui.player.NowPlayingScreen
import com.example.androidmusic.ui.player.NowPlayingTrack
import com.example.androidmusic.ui.player.PlayerUiState
import com.example.androidmusic.ui.player.QueueItemUi
import com.example.androidmusic.ui.theme.AndroidMusicTheme

private val sampleState = PlayerUiState(
    isConnected = true,
    currentTrack = NowPlayingTrack(
        id = "1",
        title = "Chop Suey!",
        artist = "System of a Down",
        album = "Toxicity",
        albumArtUri = null,
    ),
    isPlaying = true,
    positionMs = 65_000,
    durationMs = 210_000,
    isShuffleOn = true,
    repeatMode = RepeatMode.RepeatOne,
    queue = listOf(
        QueueItemUi(0, "1", "Chop Suey!", "System of a Down", isCurrent = true),
        QueueItemUi(1, "2", "Toxicity", "System of a Down", isCurrent = false),
    ),
)

@Preview(name = "Now Playing", showBackground = true, heightDp = 720)
@Composable
fun NowPlayingPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        NowPlayingScreen(state = sampleState, showQueue = false, onEvent = {})
    }
}
