package com.example.androidmusic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.error
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.ObserveLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeLibrary: ObserveLibraryUseCase,
    private val playbackController: PlaybackController,
    private val logger: Logger,
) : ViewModel() {

    // Latest domain tracks, kept so a tapped row can be played with the full
    // library as its play queue.
    @Volatile
    private var libraryTracks: List<AudioFile> = emptyList()

    val uiState: StateFlow<LibraryUiState> =
        observeLibrary()
            .onEach { libraryTracks = it.tracks }
            .map { library -> LibraryUiState(isLoading = false, tracks = library.tracks.map(AudioFile::toTrackUi)) }
            .catch { throwable ->
                logger.error(TAG, "Failed to observe library", throwable)
                emit(LibraryUiState(isLoading = false, error = "Could not load your library."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = LibraryUiState(isLoading = true),
            )

    fun onTrackClicked(trackId: String) {
        val tracks = libraryTracks
        val track = tracks.firstOrNull { it.id == trackId } ?: return
        playbackController.playSource(QueueSource.FromSingleTrack(track, tracks))
    }

    private companion object {
        const val TAG = "LibraryViewModel"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun AudioFile.toTrackUi() = TrackUi(id = id, title = title, artist = artist, album = album)
