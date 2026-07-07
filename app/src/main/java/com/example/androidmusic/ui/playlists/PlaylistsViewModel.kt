package com.example.androidmusic.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.usecase.GetPlaylistsUseCase
import com.example.androidmusic.domain.usecase.PlaylistCommands
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    getPlaylists: GetPlaylistsUseCase,
    private val commands: PlaylistCommands,
) : ViewModel() {

    val uiState: StateFlow<PlaylistsUiState> =
        getPlaylists()
            .map { playlists -> PlaylistsUiState(isLoading = false, playlists = playlists.map(Playlist::toSummary)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlaylistsUiState())

    fun onCreatePlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { commands.create(trimmed) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun Playlist.toSummary() = PlaylistSummaryUi(id = id, name = name, trackCount = entries.size)
