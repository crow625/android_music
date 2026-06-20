package com.example.androidmusic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.error
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.usecase.ObserveLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeLibrary: ObserveLibraryUseCase,
    private val logger: Logger,
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> =
        observeLibrary()
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

    private companion object {
        const val TAG = "LibraryViewModel"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun AudioFile.toTrackUi() = TrackUi(id = id, title = title, artist = artist, album = album)
