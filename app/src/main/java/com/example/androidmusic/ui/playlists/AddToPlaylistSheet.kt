package com.example.androidmusic.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Bottom sheet to add [trackId] to a playlist (or a new one). Self-contained:
 * host it behind a nullable-trackId state on any screen and clear it via [onDismiss].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    trackId: String,
    onDismiss: () -> Unit,
    viewModel: AddToPlaylistViewModel = hiltViewModel(),
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AddToPlaylistSheetContent(
            playlists = playlists,
            onSelect = { playlistId ->
                viewModel.onAddToPlaylist(trackId, playlistId)
                onDismiss()
            },
            onCreateNew = { name ->
                viewModel.onCreateAndAdd(trackId, name)
                onDismiss()
            },
        )
    }
}

/** Stateless sheet body — previewable without a ViewModel. */
@Composable
fun AddToPlaylistSheetContent(
    playlists: List<PlaylistSummaryUi>,
    onSelect: (Long) -> Unit,
    onCreateNew: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Add to playlist",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        ListItem(
            headlineContent = { Text("New playlist") },
            leadingContent = { Icon(Icons.Filled.Add, contentDescription = null) },
            modifier = Modifier.clickable { showCreateDialog = true },
        )
        HorizontalDivider()
        LazyColumn {
            items(playlists, key = { it.id }) { playlist ->
                ListItem(
                    headlineContent = { Text(playlist.name) },
                    supportingContent = { Text("${playlist.trackCount} songs") },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onSelect(playlist.id) },
                )
                HorizontalDivider()
            }
        }
    }

    if (showCreateDialog) {
        PlaylistNameDialog(
            title = "New playlist",
            confirmLabel = "Create & add",
            onConfirm = {
                onCreateNew(it)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}
