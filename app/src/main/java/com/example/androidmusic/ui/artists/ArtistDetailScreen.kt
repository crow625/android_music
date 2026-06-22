package com.example.androidmusic.ui.artists

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidmusic.ui.albums.AlbumCard

@Composable
fun ArtistDetailScreen(
    state: ArtistDetailUiState,
    onPlayArtist: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
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
            ListItem(
                headlineContent = { Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text(track.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.clickable { onTrackClick(track.id) },
            )
            HorizontalDivider()
        }
    }
}
