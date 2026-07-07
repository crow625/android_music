package com.example.androidmusic.ui.albums

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidmusic.ui.common.AlbumArtwork
import com.example.androidmusic.ui.common.DetailTrackRow
import com.example.androidmusic.ui.playlists.AddToPlaylistSheet

@Composable
fun AlbumDetailScreen(
    state: AlbumDetailUiState,
    onPlayAlbum: () -> Unit,
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addToPlaylistTrackId by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AlbumArtwork(
                artworkUri = state.artworkUri,
                modifier = Modifier.size(160.dp).align(Alignment.CenterHorizontally),
            )
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = "${state.artist} · ${state.tracks.size} songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onPlayAlbum, modifier = Modifier.padding(top = 12.dp)) {
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
