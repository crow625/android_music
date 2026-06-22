package com.example.androidmusic.ui.albums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AlbumsRoute(
    onOpenAlbum: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AlbumsScreen(state = state, onAlbumClick = onOpenAlbum, modifier = modifier)
}
