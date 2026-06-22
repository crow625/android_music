package com.example.androidmusic.ui.albums

data class AlbumUi(
    val id: String,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val artworkUri: String? = null,
)

data class AlbumsUiState(
    val isLoading: Boolean = true,
    val albums: List<AlbumUi> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && albums.isEmpty()
}

/** A track row shown in an album/artist detail list. */
data class DetailTrackUi(
    val id: String,
    val title: String,
    val subtitle: String,
)
