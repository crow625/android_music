package com.example.androidmusic.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.error
import com.example.androidmusic.domain.model.Folder
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.model.SourceFolder
import com.example.androidmusic.domain.usecase.AddSourceUseCase
import com.example.androidmusic.domain.usecase.GetFoldersUseCase
import com.example.androidmusic.domain.usecase.ObserveSourcesUseCase
import com.example.androidmusic.domain.usecase.RemoveSourceUseCase
import com.example.androidmusic.domain.usecase.ScanLibraryUseCase
import com.example.androidmusic.ui.common.folderDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    getFolders: GetFoldersUseCase,
    observeSources: ObserveSourcesUseCase,
    private val addSource: AddSourceUseCase,
    private val removeSource: RemoveSourceUseCase,
    private val scanLibrary: ScanLibraryUseCase,
    private val logger: Logger,
) : ViewModel() {

    private val scanState = MutableStateFlow(ScanState())

    val uiState: StateFlow<FoldersUiState> =
        combine(getFolders(), observeSources(), scanState) { folders, sources, scan ->
            FoldersUiState(
                isLoading = false,
                sources = sources.map(SourceFolder::toUi),
                folders = folders.map(Folder::toUi),
                isScanning = scan.isScanning,
                lastScan = scan.lastScan,
                message = scan.message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FoldersUiState())

    fun onFolderPicked(uri: MediaUri) {
        viewModelScope.launch {
            addSource(uri)
            rescan()
        }
    }

    /** Removes the source; its tracks are dropped by the repository, so the browse list updates too. */
    fun onRemoveSource(uri: String) {
        viewModelScope.launch { removeSource(MediaUri(uri)) }
    }

    private suspend fun rescan() {
        scanState.update { it.copy(isScanning = true, message = null) }
        runCatching { scanLibrary() }
            .onSuccess { report ->
                scanState.update { it.copy(isScanning = false, lastScan = report.toUi(), message = null) }
            }
            .onFailure { throwable ->
                logger.error(TAG, "Library scan failed", throwable)
                scanState.update { it.copy(isScanning = false, message = "Scan failed — see diagnostics.") }
            }
    }

    private data class ScanState(
        val isScanning: Boolean = false,
        val lastScan: ScanSummaryUi? = null,
        val message: String? = null,
    )

    private companion object {
        const val TAG = "FoldersViewModel"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun SourceFolder.toUi() = SourceUi(uri = uri.value, name = folderDisplayName(uri.value))

private fun Folder.toUi() = FolderUi(uri = uri, name = folderDisplayName(uri), trackCount = trackCount)

private fun ScanReport.toUi() = ScanSummaryUi(
    indexed = indexed,
    skipped = skippedUnsupported,
    unreadable = unreadable,
    problems = problems.size,
)
