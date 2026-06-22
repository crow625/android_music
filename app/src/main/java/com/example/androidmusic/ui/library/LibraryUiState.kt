package com.example.androidmusic.ui.library

import com.example.androidmusic.domain.model.SortOrder

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
    val query: String = "",
    val sortOrder: SortOrder = SortOrder.Title,
) {
    val hasQuery: Boolean get() = query.isNotBlank()

    /** No music at all (vs. a search that returned nothing). */
    val isEmptyLibrary: Boolean get() = !isLoading && error == null && tracks.isEmpty() && !hasQuery

    /** A non-empty search that matched nothing. */
    val isNoMatches: Boolean get() = !isLoading && error == null && tracks.isEmpty() && hasQuery
}

/** Events emitted by [LibraryScreen]. */
sealed interface LibraryEvent {
    data object OpenSources : LibraryEvent
    data class PlayTrack(val trackId: String) : LibraryEvent
    data class SetQuery(val query: String) : LibraryEvent
    data class SetSort(val sortOrder: SortOrder) : LibraryEvent
}
