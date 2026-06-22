package com.example.androidmusic.ui.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.Artist
import com.example.androidmusic.domain.usecase.GetArtistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    getArtists: GetArtistsUseCase,
) : ViewModel() {
    val uiState: StateFlow<ArtistsUiState> =
        getArtists()
            .map { artists -> ArtistsUiState(isLoading = false, artists = artists.map(Artist::toUi)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ArtistsUiState())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun Artist.toUi() = ArtistUi(id = id, name = name, albumCount = albumCount, trackCount = trackCount)
