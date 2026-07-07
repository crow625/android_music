package com.example.androidmusic.ui.playlists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlaylistDetailRoute(
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Once the playlist is gone (deleted), leave the detail screen.
    LaunchedEffect(state.isLoading, state.exists) {
        if (!state.isLoading && !state.exists) onDeleted()
    }

    PlaylistDetailScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                PlaylistDetailEvent.Play -> viewModel.onPlay()
                is PlaylistDetailEvent.Rename -> viewModel.onRename(event.name)
                PlaylistDetailEvent.Delete -> viewModel.onDelete()
                is PlaylistDetailEvent.PlayEntry -> viewModel.onPlayEntry(event.entryId)
                is PlaylistDetailEvent.RemoveEntry -> viewModel.onRemoveEntry(event.entryId)
                is PlaylistDetailEvent.Move -> viewModel.onMove(event.from, event.to)
            }
        },
        modifier = modifier,
    )
}
