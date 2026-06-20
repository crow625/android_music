package com.example.androidmusic.domain.model

/** How a list of tracks should be ordered for a library view. */
enum class SortOrder { Title, Artist, Album }

/** Any origin a [PlayQueue] can be built from. Consumed by `BuildQueueUseCase`. */
sealed interface QueueSource {
    data class FromAlbum(val albumId: String) : QueueSource
    data class FromArtist(val artistId: String) : QueueSource
    data class FromPlaylist(val playlistId: Long) : QueueSource
    data class FromFolder(val folderUri: MediaUri) : QueueSource
    data class FromLibrary(val sortOrder: SortOrder) : QueueSource
    data class FromSingleTrack(
        val file: AudioFile,
        val contextList: List<AudioFile>,
    ) : QueueSource
}
