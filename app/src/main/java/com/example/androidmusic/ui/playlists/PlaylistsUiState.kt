package com.example.androidmusic.ui.playlists

/** A playlist as shown in the list. */
data class PlaylistSummaryUi(
    val id: Long,
    val name: String,
    val trackCount: Int,
)

data class PlaylistsUiState(
    val isLoading: Boolean = true,
    val playlists: List<PlaylistSummaryUi> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && playlists.isEmpty()
}

/** One row in the playlist detail. [isResolved] == false ⇒ the file is missing (greyed). */
data class PlaylistEntryUi(
    val entryId: Long,
    val title: String,
    val subtitle: String,
    val isResolved: Boolean,
)

data class PlaylistDetailUiState(
    val isLoading: Boolean = true,
    /** False once the playlist has been deleted (drives navigate-up). */
    val exists: Boolean = true,
    val id: Long = 0,
    val name: String = "",
    val entries: List<PlaylistEntryUi> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && exists && entries.isEmpty()
    val hasResolvableTracks: Boolean get() = entries.any { it.isResolved }
}

/** Events emitted by the playlist detail screen. */
sealed interface PlaylistDetailEvent {
    data object Play : PlaylistDetailEvent
    data class Rename(val name: String) : PlaylistDetailEvent
    data object Delete : PlaylistDetailEvent
    data class PlayEntry(val entryId: Long) : PlaylistDetailEvent
    data class RemoveEntry(val entryId: Long) : PlaylistDetailEvent
    data class Move(val from: Int, val to: Int) : PlaylistDetailEvent
}
