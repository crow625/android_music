package com.example.androidmusic.ui.library

/** Lightweight display model for a track row. */
data class TrackUi(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
)

data class LibraryUiState(
    val isLoading: Boolean = true,
    val tracks: List<TrackUi> = emptyList(),
    val error: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && tracks.isEmpty()
}

/** Events emitted by [LibraryScreen]. */
sealed interface LibraryEvent {
    data object OpenSources : LibraryEvent
}
