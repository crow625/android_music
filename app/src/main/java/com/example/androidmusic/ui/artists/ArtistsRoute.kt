package com.example.androidmusic.ui.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArtistsRoute(
    onOpenArtist: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArtistsScreen(state = state, onArtistClick = onOpenArtist, modifier = modifier)
}
