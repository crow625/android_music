package com.example.androidmusic.ui.playlists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlaylistsRoute(
    onOpenPlaylist: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlaylistsScreen(
        state = state,
        onCreatePlaylist = viewModel::onCreatePlaylist,
        onOpenPlaylist = onOpenPlaylist,
        modifier = modifier,
    )
}
