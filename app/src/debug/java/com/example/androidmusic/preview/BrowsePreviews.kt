package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.albums.AlbumDetailScreen
import com.example.androidmusic.ui.albums.AlbumDetailUiState
import com.example.androidmusic.ui.albums.AlbumUi
import com.example.androidmusic.ui.albums.AlbumsScreen
import com.example.androidmusic.ui.albums.AlbumsUiState
import com.example.androidmusic.ui.albums.DetailTrackUi
import com.example.androidmusic.ui.artists.ArtistDetailScreen
import com.example.androidmusic.ui.artists.ArtistDetailUiState
import com.example.androidmusic.ui.artists.ArtistUi
import com.example.androidmusic.ui.artists.ArtistsScreen
import com.example.androidmusic.ui.artists.ArtistsUiState
import com.example.androidmusic.ui.theme.AndroidMusicTheme

private val sampleAlbums = listOf(
    AlbumUi("1", "Toxicity", "System of a Down", 14),
    AlbumUi("2", "Mezzanine", "Massive Attack", 11),
    AlbumUi("3", "OK Computer", "Radiohead", 12),
)

private val sampleTracks = listOf(
    DetailTrackUi("1", "Chop Suey!", "System of a Down"),
    DetailTrackUi("2", "Toxicity", "System of a Down"),
)

@Preview(name = "Albums grid", showBackground = true, heightDp = 500)
@Composable
fun AlbumsGridPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        AlbumsScreen(state = AlbumsUiState(isLoading = false, albums = sampleAlbums), onAlbumClick = {})
    }
}

@Preview(name = "Album detail", showBackground = true, heightDp = 500)
@Composable
fun AlbumDetailPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        AlbumDetailScreen(
            state = AlbumDetailUiState(false, "Toxicity", "System of a Down", sampleTracks),
            onPlayAlbum = {},
            onTrackClick = {},
        )
    }
}

@Preview(name = "Artists list", showBackground = true)
@Composable
fun ArtistsListPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        ArtistsScreen(
            state = ArtistsUiState(
                isLoading = false,
                artists = listOf(
                    ArtistUi("1", "System of a Down", 2, 28),
                    ArtistUi("2", "Radiohead", 3, 36),
                ),
            ),
            onArtistClick = {},
        )
    }
}

@Preview(name = "Artist detail", showBackground = true, heightDp = 600)
@Composable
fun ArtistDetailPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        ArtistDetailScreen(
            state = ArtistDetailUiState(
                isLoading = false,
                name = "System of a Down",
                albums = sampleAlbums.take(2),
                tracks = sampleTracks,
            ),
            onPlayArtist = {},
            onAlbumClick = {},
            onTrackClick = {},
        )
    }
}
