package com.example.androidmusic.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.Folder
import com.example.androidmusic.domain.usecase.GetFoldersUseCase
import com.example.androidmusic.ui.common.folderDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    getFolders: GetFoldersUseCase,
) : ViewModel() {
    val uiState: StateFlow<FoldersUiState> =
        getFolders()
            .map { folders -> FoldersUiState(isLoading = false, folders = folders.map(Folder::toUi)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FoldersUiState())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun Folder.toUi() = FolderUi(uri = uri, name = folderDisplayName(uri), trackCount = trackCount)
