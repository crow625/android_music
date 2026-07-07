package com.example.androidmusic.data.repository

import androidx.room.withTransaction
import com.example.androidmusic.data.db.AppDatabase
import com.example.androidmusic.data.db.PlaylistDao
import com.example.androidmusic.data.db.PlaylistEntity
import com.example.androidmusic.data.db.PlaylistEntryEntity
import com.example.androidmusic.data.mapper.toDomain
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.model.ResolvedEntry
import com.example.androidmusic.domain.playlist.PlaylistResolution
import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.repository.PlaylistRepository
import com.example.androidmusic.domain.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed [PlaylistRepository]. Persistence lives here; the resilient
 * resolution logic is the pure [PlaylistResolution] in `:domain`, run against the
 * current library snapshot from [AudioFileRepository].
 */
class RoomPlaylistRepository @Inject constructor(
    private val database: AppDatabase,
    private val dao: PlaylistDao,
    private val audioFileRepository: AudioFileRepository,
    private val clock: Clock,
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> =
        dao.observePlaylists().map { playlists -> playlists.map { it.toDomain() } }

    override suspend fun createPlaylist(name: String): Playlist {
        val id = dao.insertPlaylist(PlaylistEntity(name = name, createdAt = clock.now().toEpochMilli()))
        return Playlist(id = id, name = name, entries = emptyList())
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String) =
        dao.renamePlaylist(playlistId, name)

    override suspend fun deletePlaylist(playlistId: Long) =
        dao.deletePlaylist(playlistId)

    override suspend fun addTrack(playlistId: Long, track: AudioFile) {
        dao.insertEntry(
            PlaylistEntryEntity(
                playlistId = playlistId,
                trackId = track.id,
                trackTitle = track.title,
                trackArtist = track.artist,
                trackAlbum = track.album,
                filePath = track.filePath,
                position = dao.maxPosition(playlistId) + 1,
            ),
        )
    }

    override suspend fun removeTrack(playlistId: Long, entryId: Long): Unit =
        database.withTransaction {
            dao.deleteEntry(entryId)
            // Close the positional gap so positions stay contiguous (0..n-1).
            dao.updateEntries(dao.entriesFor(playlistId).mapIndexed { index, e -> e.copy(position = index) })
        }

    override suspend fun reorderTrack(playlistId: Long, from: Int, to: Int): Unit =
        database.withTransaction {
            val ordered = dao.entriesFor(playlistId).toMutableList()
            if (from !in ordered.indices || to !in ordered.indices) return@withTransaction
            ordered.add(to, ordered.removeAt(from))
            dao.updateEntries(ordered.mapIndexed { index, e -> e.copy(position = index) })
        }

    override suspend fun resolveEntries(playlistId: Long): List<ResolvedEntry> {
        val entries = dao.entriesFor(playlistId).map { it.toDomain() }
        val library = audioFileRepository.observeLibrary().first().tracks
        return PlaylistResolution.resolve(entries, library)
    }
}
