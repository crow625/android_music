package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.library.Normalize
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.model.SortOrder
import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.first

/**
 * Builds a [PlayQueue] from any [QueueSource]. Pure orchestration over the
 * repositories — no Android dependencies. The app provides it via Hilt.
 */
class BuildQueueUseCase(
    private val audioFileRepository: AudioFileRepository,
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(source: QueueSource): PlayQueue = when (source) {
        is QueueSource.FromAlbum ->
            queueOf(libraryTracks().filter { it.album == source.albumId }.sortedWith(ALBUM_ORDER))

        is QueueSource.FromArtist ->
            queueOf(libraryTracks().filter { it.artist == source.artistId }.sortedWith(ARTIST_ORDER))

        is QueueSource.FromLibrary ->
            queueOf(libraryTracks().sortedWith(comparatorFor(source.sortOrder)))

        is QueueSource.FromFolder ->
            queueOf(audioFileRepository.listAudioFiles(source.folderUri))

        is QueueSource.FromPlaylist ->
            queueOf(playlistRepository.resolveEntries(source.playlistId).mapNotNull { it.resolvedFile })

        is QueueSource.FromSingleTrack -> {
            val items = source.contextList.ifEmpty { listOf(source.file) }
            val index = items.indexOfFirst { it.id == source.file.id }.coerceAtLeast(0)
            PlayQueue(items = items, currentIndex = index)
        }
    }

    private suspend fun libraryTracks(): List<AudioFile> =
        audioFileRepository.observeLibrary().first().tracks

    private fun queueOf(tracks: List<AudioFile>): PlayQueue =
        PlayQueue(items = tracks, currentIndex = 0)

    private fun comparatorFor(order: SortOrder): Comparator<AudioFile> = when (order) {
        SortOrder.Title -> compareBy { Normalize.key(it.title) }
        SortOrder.Artist -> ARTIST_ORDER
        SortOrder.Album -> ALBUM_WITH_NAME_ORDER
    }

    private companion object {
        val ALBUM_ORDER = compareBy<AudioFile>({ it.discNumber }, { it.trackNumber })
        val ARTIST_ORDER =
            compareBy<AudioFile>({ Normalize.key(it.album) }, { it.discNumber }, { it.trackNumber })
        val ALBUM_WITH_NAME_ORDER =
            compareBy<AudioFile>({ Normalize.key(it.album) }, { it.discNumber }, { it.trackNumber })
    }
}
