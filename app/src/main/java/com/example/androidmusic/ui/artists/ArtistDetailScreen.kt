package com.example.androidmusic.ui.artists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import com.example.androidmusic.ui.albums.AlbumCard
import com.example.androidmusic.ui.common.DetailTrackRow
import com.example.androidmusic.ui.playlists.AddToPlaylistSheet

@Composable
fun ArtistDetailScreen(
    state: ArtistDetailUiState,
    onPlayArtist: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addToPlaylistTrackId by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(state.name, style = MaterialTheme.typography.headlineSmall)
                Button(onClick = onPlayArtist, modifier = Modifier.padding(top = 12.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("Play", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        if (state.albums.isNotEmpty()) {
            item {
                Text(
                    "Albums",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                )
                LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                    items(state.albums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            onClick = { onAlbumClick(album.id) },
                            modifier = Modifier.width(150.dp),
                        )
                    }
                }
            }
        }
        item {
            Text(
                "Songs",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
            )
        }
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

        addToPlaylistTrackId?.let { trackId ->
            AddToPlaylistSheet(trackId = trackId, onDismiss = { addToPlaylistTrackId = null })
        }
    }
}
