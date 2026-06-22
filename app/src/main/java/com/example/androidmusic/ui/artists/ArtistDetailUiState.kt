package com.example.androidmusic.ui.artists

import com.example.androidmusic.ui.albums.AlbumUi
import com.example.androidmusic.ui.albums.DetailTrackUi

data class ArtistDetailUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val albums: List<AlbumUi> = emptyList(),
    val tracks: List<DetailTrackUi> = emptyList(),
)
