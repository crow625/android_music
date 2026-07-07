package com.example.androidmusic.ui.folders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidmusic.ui.common.DetailTrackRow
import com.example.androidmusic.ui.playlists.AddToPlaylistSheet

@Composable
fun FolderDetailScreen(
    state: FolderDetailUiState,
    onPlayFolder: () -> Unit,
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addToPlaylistTrackId by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = state.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${state.tracks.size} songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onPlayFolder, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("Play", modifier = Modifier.padding(start = 8.dp))
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(state.tracks, key = { it.id }) { track ->
                DetailTrackRow(
                    title = track.title,
                    subtitle = track.subtitle,
                    onClick = { onTrackClick(track.id) },
                    onAddToPlaylist = { addToPlaylistTrackId = track.id },
                )
                HorizontalDivider()
            }
        }
    }

    addToPlaylistTrackId?.let { trackId ->
        AddToPlaylistSheet(trackId = trackId, onDismiss = { addToPlaylistTrackId = null })
    }
}
