package com.example.androidmusic.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.usecase.GetPlaylistsUseCase
import com.example.androidmusic.domain.usecase.ObserveLibraryUseCase
import com.example.androidmusic.domain.usecase.PlaylistCommands
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the [AddToPlaylistSheet], reachable from any track row. Resolves the
 * tapped track id against the library itself, so callers only need to pass an id
 * (works from Library, album/artist/folder detail — all subsets of the library).
 */
@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val observeLibrary: ObserveLibraryUseCase,
    getPlaylists: GetPlaylistsUseCase,
    private val commands: PlaylistCommands,
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistSummaryUi>> =
        getPlaylists()
            .map { playlists -> playlists.map { PlaylistSummaryUi(it.id, it.name, it.entries.size) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun onAddToPlaylist(trackId: String, playlistId: Long) {
        viewModelScope.launch {
            val track = trackOrNull(trackId) ?: return@launch
            commands.addTrack(playlistId, track)
        }
    }

    fun onCreateAndAdd(trackId: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val track = trackOrNull(trackId) ?: return@launch
            val playlist: Playlist = commands.create(trimmed)
            commands.addTrack(playlist.id, track)
        }
    }

    /** Resolves the id against the current library snapshot at command time. */
    private suspend fun trackOrNull(trackId: String): AudioFile? =
        observeLibrary().first().tracks.firstOrNull { it.id == trackId }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
