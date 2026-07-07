package com.example.androidmusic.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    onEvent: (PlaylistDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Header(
            state = state,
            onPlay = { onEvent(PlaylistDetailEvent.Play) },
            onRenameClick = { showRenameDialog = true },
            onDeleteClick = { showDeleteDialog = true },
        )
        if (state.isEmpty) {
            Text(
                text = "No tracks yet. Add some from your library.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(state.entries, key = { _, entry -> entry.entryId }) { index, entry ->
                EntryRow(
                    entry = entry,
                    index = index,
                    lastIndex = state.entries.lastIndex,
                    onEvent = onEvent,
                )
                HorizontalDivider()
            }
        }
    }

    if (showRenameDialog) {
        PlaylistNameDialog(
            title = "Rename playlist",
            confirmLabel = "Rename",
            initialName = state.name,
            onConfirm = {
                onEvent(PlaylistDetailEvent.Rename(it))
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete playlist?") },
            text = { Text("\"${state.name}\" will be removed. Your audio files are not affected.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onEvent(PlaylistDetailEvent.Delete)
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Header(
    state: PlaylistDetailUiState,
    onPlay: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = state.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            var menuOpen by remember { mutableStateOf(false) }
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Playlist options")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Rename") }, onClick = {
                    menuOpen = false
                    onRenameClick()
                })
                DropdownMenuItem(text = { Text("Delete playlist") }, onClick = {
                    menuOpen = false
                    onDeleteClick()
                })
            }
        }
        Text(
            text = "${state.entries.size} songs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onPlay,
            enabled = state.hasResolvableTracks,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text("Play", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun EntryRow(
    entry: PlaylistEntryUi,
    index: Int,
    lastIndex: Int,
    onEvent: (PlaylistDetailEvent) -> Unit,
) {
    // Grey the whole row when the underlying file can't be found.
    val contentColor = if (entry.isResolved) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
    }
    ListItem(
        headlineContent = { Text(entry.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            val subtitle = if (entry.isResolved) entry.subtitle else "Missing file · ${entry.subtitle}"
            Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = if (!entry.isResolved) {
            {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Unresolved",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            null
        },
        trailingContent = {
            EntryOverflow(
                canMoveUp = index > 0,
                canMoveDown = index < lastIndex,
                onMoveUp = { onEvent(PlaylistDetailEvent.Move(index, index - 1)) },
                onMoveDown = { onEvent(PlaylistDetailEvent.Move(index, index + 1)) },
                onRemove = { onEvent(PlaylistDetailEvent.RemoveEntry(entry.entryId)) },
            )
        },
        colors = ListItemDefaults.colors(
            headlineColor = contentColor,
            supportingColor = if (entry.isResolved) MaterialTheme.colorScheme.onSurfaceVariant else contentColor,
        ),
        modifier = Modifier.clickable(enabled = entry.isResolved) {
            onEvent(PlaylistDetailEvent.PlayEntry(entry.entryId))
        },
    )
}

@Composable
private fun EntryOverflow(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Track options")
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(text = { Text("Move up") }, enabled = canMoveUp, onClick = {
            open = false
            onMoveUp()
        })
        DropdownMenuItem(text = { Text("Move down") }, enabled = canMoveDown, onClick = {
            open = false
            onMoveDown()
        })
        DropdownMenuItem(text = { Text("Remove from playlist") }, onClick = {
            open = false
            onRemove()
        })
    }
}

private const val DISABLED_ALPHA = 0.38f
