package com.example.androidmusic.ui.folders

import com.example.androidmusic.ui.albums.DetailTrackUi

/** A source folder the user granted (a scan root) — removable. */
data class SourceUi(
    val uri: String,
    val name: String,
)

/** A folder that contains indexed tracks (a track's parent) — browsable. */
data class FolderUi(
    val uri: String,
    val name: String,
    val trackCount: Int,
)

data class ScanSummaryUi(
    val indexed: Int,
    val skipped: Int,
    val unreadable: Int,
    val problems: Int,
)

/**
 * Combined state for the single Folders screen: the source roots (top section,
 * with add/remove) and the browse folders derived from indexed tracks (below).
 */
data class FoldersUiState(
    val isLoading: Boolean = true,
    val sources: List<SourceUi> = emptyList(),
    val folders: List<FolderUi> = emptyList(),
    val isScanning: Boolean = false,
    val lastScan: ScanSummaryUi? = null,
    val message: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && sources.isEmpty() && folders.isEmpty()
}

sealed interface FoldersEvent {
    data object AddFolder : FoldersEvent
    data class RemoveSource(val uri: String) : FoldersEvent
    data class OpenFolder(val uri: String) : FoldersEvent
}

data class FolderDetailUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val tracks: List<DetailTrackUi> = emptyList(),
)
