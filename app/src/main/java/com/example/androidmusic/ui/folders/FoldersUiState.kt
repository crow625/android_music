package com.example.androidmusic.ui.folders

import com.example.androidmusic.ui.albums.DetailTrackUi

data class FolderUi(
    val uri: String,
    val name: String,
    val trackCount: Int,
)

data class FoldersUiState(
    val isLoading: Boolean = true,
    val folders: List<FolderUi> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && folders.isEmpty()
}

data class FolderDetailUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val tracks: List<DetailTrackUi> = emptyList(),
)
