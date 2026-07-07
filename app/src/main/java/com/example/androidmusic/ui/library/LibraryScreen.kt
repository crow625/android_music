package com.example.androidmusic.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidmusic.domain.model.SortOrder
import com.example.androidmusic.ui.playlists.AddToPlaylistSheet

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onEvent: (LibraryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Track whose "add to playlist" sheet is open (null = closed).
    var addToPlaylistTrackId by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        SearchAndSortBar(
            query = state.query,
            sortOrder = state.sortOrder,
            onQueryChange = { onEvent(LibraryEvent.SetQuery(it)) },
            onSortChange = { onEvent(LibraryEvent.SetSort(it)) },
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                )
                state.isNoMatches -> Text("No matches", modifier = Modifier.padding(24.dp))
                state.isEmptyLibrary -> EmptyLibrary(onAddFolder = { onEvent(LibraryEvent.OpenSources) })
                else -> TrackList(
                    tracks = state.tracks,
                    onTrackClick = { onEvent(LibraryEvent.PlayTrack(it)) },
                    onAddToPlaylist = { addToPlaylistTrackId = it },
                )
            }
        }
    }

    addToPlaylistTrackId?.let { trackId ->
        AddToPlaylistSheet(trackId = trackId, onDismiss = { addToPlaylistTrackId = null })
    }
}

@Composable
private fun SearchAndSortBar(
    query: String,
    sortOrder: SortOrder,
    onQueryChange: (String) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Search") },
            modifier = Modifier.weight(1f),
        )
        var menuOpen by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = { Text(order.label()) },
                        onClick = {
                            onSortChange(order)
                            menuOpen = false
                        },
                        trailingIcon = if (order == sortOrder) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<TrackUi>,
    onTrackClick: (String) -> Unit,
    onAddToPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(tracks, key = { it.id }) { track ->
            ListItem(
                headlineContent = { Text(track.title) },
                supportingContent = { Text("${track.artist} · ${track.album}") },
                trailingContent = {
                    IconButton(onClick = { onAddToPlaylist(track.id) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = "Add to playlist",
                        )
                    }
                },
                modifier = Modifier.clickable { onTrackClick(track.id) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun EmptyLibrary(onAddFolder: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("No music yet", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = "Add a folder to scan for audio files.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onAddFolder) { Text("Add a folder") }
    }
}

private fun SortOrder.label(): String = when (this) {
    SortOrder.Title -> "Title"
    SortOrder.Artist -> "Artist"
    SortOrder.Album -> "Album"
}
