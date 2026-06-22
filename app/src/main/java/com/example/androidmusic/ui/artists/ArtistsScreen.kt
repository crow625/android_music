package com.example.androidmusic.ui.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ArtistsScreen(
    state: ArtistsUiState,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.isEmpty -> Text("No artists yet", modifier = Modifier.padding(24.dp))
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.artists, key = { it.id }) { artist ->
                    ListItem(
                        headlineContent = { Text(artist.name) },
                        supportingContent = {
                            Text("${artist.albumCount} albums · ${artist.trackCount} songs")
                        },
                        modifier = Modifier.clickable { onArtistClick(artist.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
