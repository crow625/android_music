package com.example.androidmusic.ui.albums

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val artist: String = "",
    val tracks: List<DetailTrackUi> = emptyList(),
)
