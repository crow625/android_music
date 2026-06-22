package com.example.androidmusic.ui.artists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.Album
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.GetArtistAlbumsUseCase
import com.example.androidmusic.domain.usecase.GetArtistTracksUseCase
import com.example.androidmusic.ui.albums.DetailTrackUi
import com.example.androidmusic.ui.albums.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getArtistAlbums: GetArtistAlbumsUseCase,
    getArtistTracks: GetArtistTracksUseCase,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle["artistId"])

    @Volatile
    private var tracks: List<AudioFile> = emptyList()

    val uiState: StateFlow<ArtistDetailUiState> =
        combine(getArtistAlbums(artistId), getArtistTracks(artistId)) { albums, trackList ->
            tracks = trackList
            ArtistDetailUiState(
                isLoading = false,
                name = trackList.firstOrNull()?.artist ?: albums.firstOrNull()?.artist.orEmpty(),
                albums = albums.map(Album::toUi),
                tracks = trackList.map { DetailTrackUi(id = it.id, title = it.title, subtitle = it.album) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ArtistDetailUiState())

    fun onPlayArtist() = playbackController.playSource(QueueSource.FromArtist(artistId))

    fun onTrackClick(trackId: String) {
        val track = tracks.firstOrNull { it.id == trackId } ?: return
        playbackController.playSource(QueueSource.FromSingleTrack(track, tracks))
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
