package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.model.PlaylistEntry
import com.example.androidmusic.domain.model.ResolvedEntry
import com.example.androidmusic.domain.playlist.PlaylistResolution
import com.example.androidmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory [PlaylistRepository] for tests. Mutations are real (backed by a
 * [MutableStateFlow]) so ViewModel tests can observe list/detail changes.
 *
 * Resolution defaults to [PlaylistResolution] against [library]; pass
 * [resolvedByPlaylist] to override the result for a given playlist id (used by
 * `BuildQueueUseCaseTest` to inject a fixed resolution without a library).
 */
class FakePlaylistRepository(
    initial: List<Playlist> = emptyList(),
    var library: List<AudioFile> = emptyList(),
    var resolvedByPlaylist: Map<Long, List<ResolvedEntry>> = emptyMap(),
) : PlaylistRepository {

    private val state = MutableStateFlow(initial)
    private var nextPlaylistId = (initial.maxOfOrNull { it.id } ?: 0L) + 1L
    private var nextEntryId = initial.flatMap { it.entries }.maxOfOrNull { it.id }?.plus(1L) ?: 1L

    /** Convenience accessor for assertions. */
    val playlists: List<Playlist> get() = state.value

    override fun getPlaylists(): Flow<List<Playlist>> = state.asStateFlow()

    override suspend fun createPlaylist(name: String): Playlist {
        val playlist = Playlist(id = nextPlaylistId++, name = name, entries = emptyList())
        state.update { it + playlist }
        return playlist
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String) =
        state.update { lists -> lists.map { if (it.id == playlistId) it.copy(name = name) else it } }

    override suspend fun deletePlaylist(playlistId: Long) =
        state.update { lists -> lists.filterNot { it.id == playlistId } }

    override suspend fun addTrack(playlistId: Long, track: AudioFile) = mutateEntries(playlistId) { entries ->
        entries + PlaylistEntry(
            id = nextEntryId++,
            trackId = track.id,
            trackTitle = track.title,
            trackArtist = track.artist,
            trackAlbum = track.album,
            filePath = track.filePath,
            position = entries.size,
        )
    }

    override suspend fun removeTrack(playlistId: Long, entryId: Long) = mutateEntries(playlistId) { entries ->
        entries.filterNot { it.id == entryId }
    }

    override suspend fun reorderTrack(playlistId: Long, from: Int, to: Int) = mutateEntries(playlistId) { entries ->
        entries.toMutableList().apply { add(to, removeAt(from)) }
    }

    override suspend fun resolveEntries(playlistId: Long): List<ResolvedEntry> =
        resolvedByPlaylist[playlistId]
            ?: PlaylistResolution.resolve(entriesOf(playlistId), library)

    private fun entriesOf(playlistId: Long): List<PlaylistEntry> =
        state.value.firstOrNull { it.id == playlistId }?.entries.orEmpty()

    private fun mutateEntries(playlistId: Long, transform: (List<PlaylistEntry>) -> List<PlaylistEntry>) =
        state.update { lists ->
            lists.map { playlist ->
                if (playlist.id != playlistId) {
                    playlist
                } else {
                    val reindexed = transform(playlist.entries)
                        .mapIndexed { index, entry -> entry.copy(position = index) }
                    playlist.copy(entries = reindexed)
                }
            }
        }
}
