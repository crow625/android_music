package com.example.androidmusic.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FoldersScreen(
    state: FoldersUiState,
    onFolderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.isEmpty -> Text("No folders yet", modifier = Modifier.padding(24.dp))
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.folders, key = { it.uri }) { folder ->
                    ListItem(
                        headlineContent = { Text(folder.name) },
                        supportingContent = { Text("${folder.trackCount} songs") },
                        leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                        modifier = Modifier.clickable { onFolderClick(folder.uri) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
