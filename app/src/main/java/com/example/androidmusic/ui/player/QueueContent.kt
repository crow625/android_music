package com.example.androidmusic.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** The list shown inside the queue bottom sheet. Factored out so it can be previewed. */
@Composable
fun QueueContent(
    queue: List<QueueItemUi>,
    onJumpTo: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Up next", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onClear) { Text("Clear queue") }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
            items(queue, key = { it.index }) { item ->
                ListItem(
                    headlineContent = {
                        Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        Text(item.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    leadingContent = if (item.isCurrent) {
                        { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Now playing") }
                    } else {
                        null
                    },
                    trailingContent = {
                        IconButton(onClick = { onRemove(item.index) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove from queue")
                        }
                    },
                    modifier = Modifier.clickable { onJumpTo(item.index) },
                )
                HorizontalDivider()
            }
        }
    }
}
