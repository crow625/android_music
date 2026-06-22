package com.example.androidmusic.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.Album
import com.example.androidmusic.domain.usecase.GetAlbumsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    getAlbums: GetAlbumsUseCase,
) : ViewModel() {
    val uiState: StateFlow<AlbumsUiState> =
        getAlbums()
            .map { albums -> AlbumsUiState(isLoading = false, albums = albums.map(Album::toUi)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AlbumsUiState())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

internal fun Album.toUi() =
    AlbumUi(id = id, title = title, artist = artist, trackCount = trackCount, artworkUri = artworkUri)
