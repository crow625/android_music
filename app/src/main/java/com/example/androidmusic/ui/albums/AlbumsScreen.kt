package com.example.androidmusic.ui.albums

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlbumsScreen(
    state: AlbumsUiState,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.isEmpty -> Text("No albums yet", modifier = Modifier.padding(24.dp))
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.albums, key = { it.id }) { album ->
                    AlbumCard(album = album, onClick = { onAlbumClick(album.id) })
                }
            }
        }
    }
}
