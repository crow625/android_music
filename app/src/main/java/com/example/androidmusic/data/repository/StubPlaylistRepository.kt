package com.example.androidmusic.data.repository

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.model.ResolvedEntry
import com.example.androidmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Placeholder so [BuildQueueUseCase] can be constructed before playlists exist.
 * Replaced by a real Room-backed implementation in Phase 5.
 */
class StubPlaylistRepository @Inject constructor() : PlaylistRepository {
    override fun getPlaylists(): Flow<List<Playlist>> = flowOf(emptyList())
    override suspend fun createPlaylist(name: String): Playlist = Playlist(0, name, emptyList())
    override suspend fun addTrack(playlistId: Long, track: AudioFile) = Unit
    override suspend fun removeTrack(playlistId: Long, entryId: Long) = Unit
    override suspend fun reorderTrack(playlistId: Long, from: Int, to: Int) = Unit
    override suspend fun resolveEntries(playlistId: Long): List<ResolvedEntry> = emptyList()
}
