package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.model.ResolvedPlaylist
import com.example.androidmusic.domain.playlist.PlaylistResolution
import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** All playlists (with their stored entries) for the list screen. */
class GetPlaylistsUseCase(private val playlistRepository: PlaylistRepository) {
    operator fun invoke(): Flow<List<Playlist>> = playlistRepository.getPlaylists()
}

/**
 * One playlist, reactively resolved against the live library so titles and the
 * resolved/unresolved state update as the library changes. Emits `null` when the
 * playlist no longer exists (e.g. just deleted).
 */
class ObservePlaylistUseCase(
    private val playlistRepository: PlaylistRepository,
    private val audioFileRepository: AudioFileRepository,
) {
    operator fun invoke(playlistId: Long): Flow<ResolvedPlaylist?> =
        combine(
            playlistRepository.getPlaylists(),
            audioFileRepository.observeLibrary(),
        ) { playlists, library ->
            playlists.firstOrNull { it.id == playlistId }?.let { playlist ->
                ResolvedPlaylist(
                    id = playlist.id,
                    name = playlist.name,
                    entries = PlaylistResolution.resolve(playlist.entries, library.tracks),
                )
            }
        }
}

/**
 * Playlist mutations, grouped behind one seam. These are thin, well-defined
 * commands over [PlaylistRepository]; bundling them keeps ViewModels from taking
 * a long list of near-identical use-case dependencies.
 */
class PlaylistCommands(private val playlistRepository: PlaylistRepository) {
    suspend fun create(name: String): Playlist = playlistRepository.createPlaylist(name)
    suspend fun rename(playlistId: Long, name: String) = playlistRepository.renamePlaylist(playlistId, name)
    suspend fun delete(playlistId: Long) = playlistRepository.deletePlaylist(playlistId)
    suspend fun addTrack(playlistId: Long, track: AudioFile) = playlistRepository.addTrack(playlistId, track)
    suspend fun removeTrack(playlistId: Long, entryId: Long) = playlistRepository.removeTrack(playlistId, entryId)
    suspend fun reorder(playlistId: Long, from: Int, to: Int) = playlistRepository.reorderTrack(playlistId, from, to)
}
