package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.albums.DetailTrackUi
import com.example.androidmusic.ui.folders.FolderDetailScreen
import com.example.androidmusic.ui.folders.FolderDetailUiState
import com.example.androidmusic.ui.folders.FolderUi
import com.example.androidmusic.ui.folders.FoldersScreen
import com.example.androidmusic.ui.folders.FoldersUiState
import com.example.androidmusic.ui.theme.AndroidMusicTheme

@Preview(name = "Folders list", showBackground = true)
@Composable
fun FoldersListPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        FoldersScreen(
            state = FoldersUiState(
                isLoading = false,
                folders = listOf(
                    FolderUi("tree://a", "Rock", 24),
                    FolderUi("tree://b", "Jazz", 11),
                ),
            ),
            onFolderClick = {},
        )
    }
}

@Preview(name = "Folder detail", showBackground = true, heightDp = 500)
@Composable
fun FolderDetailPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        FolderDetailScreen(
            state = FolderDetailUiState(
                isLoading = false,
                name = "Rock",
                tracks = listOf(
                    DetailTrackUi("1", "Aerials", "System of a Down"),
                    DetailTrackUi("2", "Teardrop", "Massive Attack"),
                ),
            ),
            onPlayFolder = {},
            onTrackClick = {},
        )
    }
}
