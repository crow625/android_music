package com.example.androidmusic.domain.repository

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.model.ResolvedEntry
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Playlist
    suspend fun addTrack(playlistId: Long, track: AudioFile)
    suspend fun removeTrack(playlistId: Long, entryId: Long)
    suspend fun reorderTrack(playlistId: Long, from: Int, to: Int)
    suspend fun resolveEntries(playlistId: Long): List<ResolvedEntry>
}
