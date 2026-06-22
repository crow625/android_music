package com.example.androidmusic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.error
import com.example.androidmusic.domain.library.LibraryQuery
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.model.SortOrder
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.ObserveLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeLibrary: ObserveLibraryUseCase,
    private val playbackController: PlaybackController,
    private val logger: Logger,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder.Title)

    // The currently-displayed (filtered + sorted) tracks, used as the play queue
    // context when a row is tapped.
    @Volatile
    private var displayed: List<AudioFile> = emptyList()

    val uiState: StateFlow<LibraryUiState> =
        combine(observeLibrary(), query, sortOrder) { library, currentQuery, currentSort ->
            val tracks = LibraryQuery.apply(library.tracks, currentQuery, currentSort)
            displayed = tracks
            LibraryUiState(
                isLoading = false,
                tracks = tracks.map(AudioFile::toTrackUi),
                query = currentQuery,
                sortOrder = currentSort,
            )
        }.catch { throwable ->
            logger.error(TAG, "Failed to observe library", throwable)
            emit(LibraryUiState(isLoading = false, error = "Could not load your library."))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = LibraryUiState(isLoading = true),
        )

    fun onSearchChange(value: String) {
        query.value = value
    }

    fun onSortChange(order: SortOrder) {
        sortOrder.value = order
    }

    fun onTrackClicked(trackId: String) {
        val track = displayed.firstOrNull { it.id == trackId } ?: return
        playbackController.playSource(QueueSource.FromSingleTrack(track, displayed))
    }

    private companion object {
        const val TAG = "LibraryViewModel"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun AudioFile.toTrackUi() = TrackUi(id = id, title = title, artist = artist, album = album)
