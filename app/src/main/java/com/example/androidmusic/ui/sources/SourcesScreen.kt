package com.example.androidmusic.ui.sources

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Stateless Folder Sources screen. */
@Composable
fun SourcesScreen(
    state: SourcesUiState,
    onEvent: (SourcesEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { onEvent(SourcesEvent.AddFolder) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = null)
            Text(text = "Add folder", modifier = Modifier.padding(start = 8.dp))
        }

        if (state.isScanning) {
            Text("Scanning…", modifier = Modifier.padding(top = 16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        }

        state.lastScan?.let { summary ->
            Text(
                text = "Indexed ${summary.indexed} · skipped ${summary.skipped} · " +
                    "unreadable ${summary.unreadable} · problems ${summary.problems}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        state.message?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (state.sources.isEmpty()) {
            Text(
                text = "No source folders yet.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                items(state.sources, key = { it.uri }) { source ->
                    ListItem(
                        headlineContent = { Text(source.displayName) },
                        trailingContent = {
                            IconButton(onClick = { onEvent(SourcesEvent.RemoveSource(source.uri)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        },
                    )
                }
            }
        }
    }
}
