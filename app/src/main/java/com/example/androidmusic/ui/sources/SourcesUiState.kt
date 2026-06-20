package com.example.androidmusic.ui.sources

data class SourceUi(
    val uri: String,
    val displayName: String,
)

data class ScanSummaryUi(
    val indexed: Int,
    val skipped: Int,
    val unreadable: Int,
    val problems: Int,
)

data class SourcesUiState(
    val sources: List<SourceUi> = emptyList(),
    val isScanning: Boolean = false,
    val lastScan: ScanSummaryUi? = null,
    val message: String? = null,
)

sealed interface SourcesEvent {
    data object AddFolder : SourcesEvent
    data object Rescan : SourcesEvent
    data class RemoveSource(val uri: String) : SourcesEvent
}
