package com.example.androidmusic.preview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.playlists.AddToPlaylistSheetContent
import com.example.androidmusic.ui.playlists.PlaylistDetailScreen
import com.example.androidmusic.ui.playlists.PlaylistDetailUiState
import com.example.androidmusic.ui.playlists.PlaylistEntryUi
import com.example.androidmusic.ui.playlists.PlaylistSummaryUi
import com.example.androidmusic.ui.playlists.PlaylistsScreen
import com.example.androidmusic.ui.playlists.PlaylistsUiState
import com.example.androidmusic.ui.theme.AndroidMusicTheme

@Preview(name = "Playlists list", showBackground = true)
@Composable
fun PlaylistsListPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        PlaylistsScreen(
            state = PlaylistsUiState(
                isLoading = false,
                playlists = listOf(
                    PlaylistSummaryUi(1, "Late Night", 12),
                    PlaylistSummaryUi(2, "Focus", 34),
                    PlaylistSummaryUi(3, "Roadtrip", 88),
                ),
            ),
            onCreatePlaylist = {},
            onOpenPlaylist = {},
        )
    }
}

@Preview(name = "Playlists empty", showBackground = true)
@Composable
fun PlaylistsEmptyPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        PlaylistsScreen(
            state = PlaylistsUiState(isLoading = false, playlists = emptyList()),
            onCreatePlaylist = {},
            onOpenPlaylist = {},
        )
    }
}

@Preview(name = "Playlist detail (with unresolved)", showBackground = true, heightDp = 520)
@Composable
fun PlaylistDetailPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        PlaylistDetailScreen(
            state = PlaylistDetailUiState(
                isLoading = false,
                id = 1,
                name = "Late Night",
                entries = listOf(
                    PlaylistEntryUi(1, "Teardrop", "Massive Attack", isResolved = true),
                    PlaylistEntryUi(2, "Glory Box", "Portishead", isResolved = true),
                    PlaylistEntryUi(3, "Moved Track", "Unknown Artist", isResolved = false),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Add to playlist sheet", showBackground = true, heightDp = 360)
@Composable
fun AddToPlaylistSheetPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        AddToPlaylistSheetContent(
            playlists = listOf(
                PlaylistSummaryUi(1, "Late Night", 12),
                PlaylistSummaryUi(2, "Focus", 34),
            ),
            onSelect = {},
            onCreateNew = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
