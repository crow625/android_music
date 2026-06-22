package com.example.androidmusic.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LibraryRoute(
    onOpenSources: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                LibraryEvent.OpenSources -> onOpenSources()
                is LibraryEvent.PlayTrack -> viewModel.onTrackClicked(event.trackId)
                is LibraryEvent.SetQuery -> viewModel.onSearchChange(event.query)
                is LibraryEvent.SetSort -> viewModel.onSortChange(event.sortOrder)
            }
        },
        modifier = modifier,
    )
}
