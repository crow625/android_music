package com.example.androidmusic.ui.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArtistDetailRoute(
    onOpenAlbum: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArtistDetailScreen(
        state = state,
        onPlayArtist = viewModel::onPlayArtist,
        onAlbumClick = onOpenAlbum,
        onTrackClick = viewModel::onTrackClick,
        modifier = modifier,
    )
}
