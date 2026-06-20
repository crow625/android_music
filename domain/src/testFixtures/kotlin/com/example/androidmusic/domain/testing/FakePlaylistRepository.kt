package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.model.ResolvedEntry
import com.example.androidmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePlaylistRepository(
    var playlists: List<Playlist> = emptyList(),
    var resolvedByPlaylist: Map<Long, List<ResolvedEntry>> = emptyMap(),
) : PlaylistRepository {
    private var nextId = 1L

    override fun getPlaylists(): Flow<List<Playlist>> = flowOf(playlists)

    override suspend fun createPlaylist(name: String): Playlist =
        Playlist(id = nextId++, name = name, entries = emptyList()).also { playlists = playlists + it }

    override suspend fun addTrack(playlistId: Long, track: AudioFile) = Unit
    override suspend fun removeTrack(playlistId: Long, entryId: Long) = Unit
    override suspend fun reorderTrack(playlistId: Long, from: Int, to: Int) = Unit

    override suspend fun resolveEntries(playlistId: Long): List<ResolvedEntry> =
        resolvedByPlaylist[playlistId].orEmpty()
}
