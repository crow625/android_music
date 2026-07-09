package com.example.androidmusic.ui.folders

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidmusic.domain.model.MediaUri

@Composable
fun FoldersRoute(
    onOpenFolder: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoldersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val pickFolder = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) viewModel.onFolderPicked(MediaUri(uri.toString()))
    }

    FoldersScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                FoldersEvent.AddFolder -> pickFolder.launch(null)
                is FoldersEvent.RemoveSource -> viewModel.onRemoveSource(event.uri)
                is FoldersEvent.OpenFolder -> onOpenFolder(event.uri)
            }
        },
        modifier = modifier,
    )
}
