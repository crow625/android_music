package com.example.androidmusic.ui.artists

data class ArtistUi(
    val id: String,
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
)

data class ArtistsUiState(
    val isLoading: Boolean = true,
    val artists: List<ArtistUi> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && artists.isEmpty()
}
