package com.example.androidmusic.ui.albums

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.GetAlbumTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getAlbumTracks: GetAlbumTracksUseCase,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private val albumId: String = checkNotNull(savedStateHandle["albumId"])

    @Volatile
    private var tracks: List<AudioFile> = emptyList()

    val uiState: StateFlow<AlbumDetailUiState> =
        getAlbumTracks(albumId)
            .onEach { tracks = it }
            .map { it.toAlbumDetailUiState() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AlbumDetailUiState())

    fun onPlayAlbum() = playbackController.playSource(QueueSource.FromAlbum(albumId))

    fun onTrackClick(trackId: String) {
        val track = tracks.firstOrNull { it.id == trackId } ?: return
        playbackController.playSource(QueueSource.FromSingleTrack(track, tracks))
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun List<AudioFile>.toAlbumDetailUiState(): AlbumDetailUiState {
    val first = firstOrNull()
    return AlbumDetailUiState(
        isLoading = false,
        title = first?.album.orEmpty(),
        artist = first?.let { it.albumArtist.ifBlank { it.artist } }.orEmpty(),
        tracks = map { DetailTrackUi(id = it.id, title = it.title, subtitle = it.artist) },
    )
}
