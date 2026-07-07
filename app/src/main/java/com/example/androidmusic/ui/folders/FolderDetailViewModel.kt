package com.example.androidmusic.ui.folders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.GetFolderTracksUseCase
import com.example.androidmusic.ui.albums.DetailTrackUi
import com.example.androidmusic.ui.common.folderDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getFolderTracks: GetFolderTracksUseCase,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private val folderUri: String = checkNotNull(savedStateHandle["folderUri"])

    @Volatile
    private var tracks: List<AudioFile> = emptyList()

    val uiState: StateFlow<FolderDetailUiState> =
        getFolderTracks(folderUri)
            .onEach { tracks = it }
            .map { list ->
                FolderDetailUiState(
                    isLoading = false,
                    name = folderDisplayName(folderUri),
                    tracks = list.map { DetailTrackUi(id = it.id, title = it.title, subtitle = it.artist) },
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FolderDetailUiState())

    fun onPlayFolder() = playbackController.playSource(QueueSource.FromFolder(MediaUri(folderUri)))

    fun onTrackClick(trackId: String) {
        val track = tracks.firstOrNull { it.id == trackId } ?: return
        playbackController.playSource(QueueSource.FromSingleTrack(track, tracks))
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
