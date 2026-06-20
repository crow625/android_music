package com.example.androidmusic.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.error
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.model.SourceFolder
import com.example.androidmusic.domain.usecase.AddSourceUseCase
import com.example.androidmusic.domain.usecase.ObserveSourcesUseCase
import com.example.androidmusic.domain.usecase.RemoveSourceUseCase
import com.example.androidmusic.domain.usecase.ScanLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    observeSources: ObserveSourcesUseCase,
    private val addSource: AddSourceUseCase,
    private val removeSource: RemoveSourceUseCase,
    private val scanLibrary: ScanLibraryUseCase,
    private val logger: Logger,
) : ViewModel() {

    private val scanState = MutableStateFlow(ScanState())

    val uiState: StateFlow<SourcesUiState> =
        combine(observeSources(), scanState) { sources, scan ->
            SourcesUiState(
                sources = sources.map(SourceFolder::toUi),
                isScanning = scan.isScanning,
                lastScan = scan.lastScan,
                message = scan.message,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SourcesUiState(),
        )

    fun onFolderPicked(uri: MediaUri) {
        viewModelScope.launch {
            addSource(uri)
            rescan()
        }
    }

    fun onRemoveSource(uri: String) {
        viewModelScope.launch { removeSource(MediaUri(uri)) }
    }

    fun onRescan() {
        viewModelScope.launch { rescan() }
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
        const val TAG = "SourcesViewModel"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun SourceFolder.toUi() = SourceUi(uri = uri.value, displayName = displayName(uri.value))

private fun ScanReport.toUi() = ScanSummaryUi(
    indexed = indexed,
    skipped = skippedUnsupported,
    unreadable = unreadable,
    problems = problems.size,
)

private fun displayName(uriString: String): String {
    val tail = uriString.substringAfterLast("%3A").substringAfterLast('/')
    return runCatching { URLDecoder.decode(tail, "UTF-8") }.getOrDefault(tail)
}
