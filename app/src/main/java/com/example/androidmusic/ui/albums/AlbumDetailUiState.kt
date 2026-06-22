package com.example.androidmusic.ui.albums

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val artist: String = "",
    val artworkUri: String? = null,
    val tracks: List<DetailTrackUi> = emptyList(),
)
