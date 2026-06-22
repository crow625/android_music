package com.example.androidmusic.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidmusic.domain.model.RepeatMode
import com.example.androidmusic.ui.common.AlbumArtwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: PlayerUiState,
    showQueue: Boolean,
    onEvent: (NowPlayingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.currentTrack
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlbumArtwork(
            artworkUri = track?.artworkUri,
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(0.8f).aspectRatio(1f),
        )

        Text(
            text = track?.title ?: "Nothing playing",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        )
        Text(
            text = listOfNotNull(track?.artist, track?.album).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )

        SeekBar(
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            onSeek = { onEvent(NowPlayingEvent.Seek(it)) },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        )

        Controls(
            isPlaying = state.isPlaying,
            isShuffleOn = state.isShuffleOn,
            repeatMode = state.repeatMode,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        IconButton(
            onClick = { onEvent(NowPlayingEvent.ToggleQueue) },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Queue")
        }
    }

    if (showQueue) {
        ModalBottomSheet(onDismissRequest = { onEvent(NowPlayingEvent.ToggleQueue) }) {
            QueueContent(
                queue = state.queue,
                onJumpTo = { onEvent(NowPlayingEvent.JumpTo(it)) },
                onRemove = { onEvent(NowPlayingEvent.Remove(it)) },
                onClear = { onEvent(NowPlayingEvent.ClearQueue) },
            )
        }
    }
}

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    val sliderValue = if (dragging) dragValue else positionMs.toFloat()
    val range = 0f..durationMs.coerceAtLeast(1L).toFloat()

    Column(modifier = modifier) {
        Slider(
            value = sliderValue.coerceIn(range.start, range.endInclusive),
            onValueChange = { dragging = true; dragValue = it },
            onValueChangeFinished = {
                onSeek(dragValue.toLong())
                dragging = false
            },
            valueRange = range,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(sliderValue.toLong()), style = MaterialTheme.typography.labelMedium)
            Text("-" + formatTime(durationMs - sliderValue.toLong()), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Controls(
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    repeatMode: RepeatMode,
    onEvent: (NowPlayingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTint = MaterialTheme.colorScheme.primary
    val inactiveTint = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onEvent(NowPlayingEvent.ToggleShuffle) }) {
            Icon(
                Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffleOn) activeTint else inactiveTint,
            )
        }
        IconButton(onClick = { onEvent(NowPlayingEvent.Previous) }) {
            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
        }
        IconButton(onClick = { onEvent(NowPlayingEvent.PlayPause) }, modifier = Modifier.size(64.dp)) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(48.dp),
            )
        }
        IconButton(onClick = { onEvent(NowPlayingEvent.Next) }) {
            Icon(Icons.Filled.SkipNext, contentDescription = "Next")
        }
        IconButton(onClick = { onEvent(NowPlayingEvent.CycleRepeat) }) {
            Icon(
                imageVector = repeatMode.icon(),
                contentDescription = "Repeat",
                tint = if (repeatMode == RepeatMode.Off) inactiveTint else activeTint,
            )
        }
    }
}

private fun RepeatMode.icon(): ImageVector = when (this) {
    RepeatMode.RepeatOne -> Icons.Filled.RepeatOne
    else -> Icons.Filled.Repeat
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
