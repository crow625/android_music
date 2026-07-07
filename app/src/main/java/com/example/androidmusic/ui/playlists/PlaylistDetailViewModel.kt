package com.example.androidmusic.ui.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.model.ResolvedEntry
import com.example.androidmusic.domain.model.ResolvedPlaylist
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.ObservePlaylistUseCase
import com.example.androidmusic.domain.usecase.PlaylistCommands
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlaylist: ObservePlaylistUseCase,
    private val commands: PlaylistCommands,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle.get<String>("playlistId")).toLong()

    // Latest resolution snapshot, used to turn an entry tap into a play queue.
    @Volatile
    private var resolvedEntries: List<ResolvedEntry> = emptyList()

    val uiState: StateFlow<PlaylistDetailUiState> =
        observePlaylist(playlistId)
            .onEach { resolvedEntries = it?.entries.orEmpty() }
            .map { it?.toUi() ?: PlaylistDetailUiState(isLoading = false, exists = false) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlaylistDetailUiState())

    fun onPlay() = playbackController.playSource(QueueSource.FromPlaylist(playlistId))

    fun onPlayEntry(entryId: Long) {
        val target = resolvedEntries.firstOrNull { it.entry.id == entryId }?.resolvedFile ?: return
        val playable = resolvedEntries.mapNotNull { it.resolvedFile }
        playbackController.playSource(QueueSource.FromSingleTrack(target, playable))
    }

    fun onRename(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { commands.rename(playlistId, trimmed) }
    }

    fun onDelete() {
        viewModelScope.launch { commands.delete(playlistId) }
    }

    fun onRemoveEntry(entryId: Long) {
        viewModelScope.launch { commands.removeTrack(playlistId, entryId) }
    }

    fun onMove(from: Int, to: Int) {
        viewModelScope.launch { commands.reorder(playlistId, from, to) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun ResolvedPlaylist.toUi(): PlaylistDetailUiState = PlaylistDetailUiState(
    isLoading = false,
    exists = true,
    id = id,
    name = name,
    entries = entries.map { resolved ->
        PlaylistEntryUi(
            entryId = resolved.entry.id,
            // Prefer live library metadata; fall back to what was captured at add-time.
            title = resolved.resolvedFile?.title ?: resolved.entry.trackTitle,
            subtitle = resolved.resolvedFile?.artist ?: resolved.entry.trackArtist,
            isResolved = resolved.isResolved,
        )
    },
)
