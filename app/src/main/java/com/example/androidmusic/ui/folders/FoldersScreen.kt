package com.example.androidmusic.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp

@Composable
fun FoldersScreen(
    state: FoldersUiState,
    onEvent: (FoldersEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sourcePendingRemoval by remember { mutableStateOf<SourceUi?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp)) }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                scanStatus(state)

                if (state.sources.isNotEmpty()) {
                    sectionHeader("Sources")
                    items(state.sources, key = { "src:${it.uri}" }) { source ->
                        ListItem(
                            headlineContent = { Text(source.name) },
                            leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                            trailingContent = {
                                IconButton(onClick = { sourcePendingRemoval = source }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove source")
                                }
                            },
                        )
                        HorizontalDivider()
                    }
                }

                sectionHeader("Folders")
                if (state.folders.isEmpty()) {
                    item {
                        Text(
                            text = "No folders with tracks yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    items(state.folders, key = { "dir:${it.uri}" }) { folder ->
                        ListItem(
                            headlineContent = { Text(folder.name) },
                            supportingContent = { Text("${folder.trackCount} songs") },
                            leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                            modifier = Modifier.clickable { onEvent(FoldersEvent.OpenFolder(folder.uri)) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        HorizontalDivider()
        Button(
            onClick = { onEvent(FoldersEvent.AddFolder) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Icon(Icons.Filled.CreateNewFolder, contentDescription = null)
            Text("Add folder", modifier = Modifier.padding(start = 8.dp))
        }
    }

    sourcePendingRemoval?.let { source ->
        AlertDialog(
            onDismissRequest = { sourcePendingRemoval = null },
            title = { Text("Remove source folder?") },
            text = {
                Text(
                    "\"${source.name}\" and its tracks will be removed from the library. " +
                        "The files on disk are not deleted.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(FoldersEvent.RemoveSource(source.uri))
                    sourcePendingRemoval = null
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { sourcePendingRemoval = null }) { Text("Cancel") } },
        )
    }
}

private fun LazyListScope.sectionHeader(title: String) {
    item {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
        )
    }
}

private fun LazyListScope.scanStatus(state: FoldersUiState) {
    if (state.isScanning) {
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Scanning…", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }
        }
    }
    state.lastScan?.let { summary ->
        item {
            Text(
                text = "Indexed ${summary.indexed} · skipped ${summary.skipped} · " +
                    "unreadable ${summary.unreadable} · problems ${summary.problems}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
    state.message?.let { message ->
        item {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}
