package com.example.androidmusic.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    onEvent: (PlaylistDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var entryPendingRemoval by remember { mutableStateOf<PlaylistEntryUi?>(null) }

    // Local, optimistic copy so a drag animates instantly; the net move is
    // persisted on drop and the upstream state resyncs this list afterwards.
    var entries by remember { mutableStateOf(state.entries) }
    LaunchedEffect(state.entries) { entries = state.entries }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        entries = entries.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }
    // Stable key of the row being dragged; used to compute the net move on drop
    // (the per-item index is captured at drag start and goes stale mid-drag).
    var draggedKey by remember { mutableStateOf<Long?>(null) }

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
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(entries, key = { _, entry -> entry.entryId }) { _, entry ->
                ReorderableItem(reorderState, key = entry.entryId) { _ ->
                    val handleModifier = Modifier.draggableHandle(
                        onDragStarted = { draggedKey = entry.entryId },
                        onDragStopped = { persistMove(draggedKey, state.entries, entries, onEvent) },
                    )
                    EntryRow(
                        entry = entry,
                        handleModifier = handleModifier,
                        onClick = { onEvent(PlaylistDetailEvent.PlayEntry(entry.entryId)) },
                        onRemove = { entryPendingRemoval = entry },
                    )
                    HorizontalDivider()
                }
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
        ConfirmDialog(
            title = "Delete playlist?",
            body = "\"${state.name}\" will be removed. Your audio files are not affected.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteDialog = false
                onEvent(PlaylistDetailEvent.Delete)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
    entryPendingRemoval?.let { entry ->
        ConfirmDialog(
            title = "Remove from playlist?",
            body = "\"${entry.title}\" will be removed from this playlist.",
            confirmLabel = "Remove",
            onConfirm = {
                onEvent(PlaylistDetailEvent.RemoveEntry(entry.entryId))
                entryPendingRemoval = null
            },
            onDismiss = { entryPendingRemoval = null },
        )
    }
}

/**
 * Translates the visual drag result into a single persisted move: the dragged
 * key's position in the persisted order ([persisted]) → its position in the
 * locally reordered [local] list. No-op if unchanged.
 */
private fun persistMove(
    draggedKey: Long?,
    persisted: List<PlaylistEntryUi>,
    local: List<PlaylistEntryUi>,
    onEvent: (PlaylistDetailEvent) -> Unit,
) {
    val key = draggedKey ?: return
    val from = persisted.indexOfFirst { it.entryId == key }
    val to = local.indexOfFirst { it.entryId == key }
    if (from >= 0 && to >= 0 && from != to) onEvent(PlaylistDetailEvent.Move(from, to))
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
            // Box anchors the dropdown under the overflow button, not the whole row.
            Box {
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
    handleModifier: Modifier,
    onClick: () -> Unit,
    onRemove: () -> Unit,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove from playlist")
                }
                Icon(
                    Icons.Filled.DragHandle,
                    contentDescription = "Reorder",
                    modifier = handleModifier,
                )
            }
        },
        colors = ListItemDefaults.colors(
            headlineColor = contentColor,
            supportingColor = if (entry.isResolved) MaterialTheme.colorScheme.onSurfaceVariant else contentColor,
        ),
        modifier = Modifier.clickable(enabled = entry.isResolved, onClick = onClick),
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private const val DISABLED_ALPHA = 0.38f
