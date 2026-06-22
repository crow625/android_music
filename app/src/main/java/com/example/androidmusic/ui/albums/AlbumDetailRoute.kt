package com.example.androidmusic.ui.albums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AlbumDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AlbumDetailScreen(
        state = state,
        onPlayAlbum = viewModel::onPlayAlbum,
        onTrackClick = viewModel::onTrackClick,
        modifier = modifier,
    )
}
