package com.example.androidmusic.ui.folders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FolderDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: FolderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FolderDetailScreen(
        state = state,
        onPlayFolder = viewModel::onPlayFolder,
        onTrackClick = viewModel::onTrackClick,
        modifier = modifier,
    )
}
