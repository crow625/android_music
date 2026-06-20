package com.example.androidmusic.ui.sources

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidmusic.domain.model.MediaUri

@Composable
fun SourcesRoute(
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val pickFolder = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            viewModel.onFolderPicked(MediaUri(uri.toString()))
        }
    }

    SourcesScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                SourcesEvent.AddFolder -> pickFolder.launch(null)
                SourcesEvent.Rescan -> viewModel.onRescan()
                is SourcesEvent.RemoveSource -> viewModel.onRemoveSource(event.uri)
            }
        },
        modifier = modifier,
    )
}
