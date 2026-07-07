package com.example.androidmusic.ui.folders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FoldersRoute(
    onOpenFolder: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoldersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FoldersScreen(state = state, onFolderClick = onOpenFolder, modifier = modifier)
}
