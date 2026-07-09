package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.albums.DetailTrackUi
import com.example.androidmusic.ui.folders.FolderDetailScreen
import com.example.androidmusic.ui.folders.FolderDetailUiState
import com.example.androidmusic.ui.folders.FolderUi
import com.example.androidmusic.ui.folders.FoldersScreen
import com.example.androidmusic.ui.folders.FoldersUiState
import com.example.androidmusic.ui.folders.ScanSummaryUi
import com.example.androidmusic.ui.folders.SourceUi
import com.example.androidmusic.ui.theme.AndroidMusicTheme

@Preview(name = "Folders – sources + folders", showBackground = true, heightDp = 560)
@Composable
fun FoldersPopulatedPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        FoldersScreen(
            state = FoldersUiState(
                isLoading = false,
                sources = listOf(
                    SourceUi("tree://primary%3AMusic", "Music"),
                    SourceUi("tree://sdcard%3APodcasts", "Podcasts"),
                ),
                folders = listOf(
                    FolderUi("tree://a", "Jazz", 12),
                    FolderUi("tree://b", "Rock", 24),
                ),
                lastScan = ScanSummaryUi(indexed = 128, skipped = 3, unreadable = 1, problems = 1),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Folders – empty", showBackground = true)
@Composable
fun FoldersEmptyPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        FoldersScreen(state = FoldersUiState(isLoading = false), onEvent = {})
    }
}

@Preview(name = "Folders – scanning", showBackground = true)
@Composable
fun FoldersScanningPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        FoldersScreen(
            state = FoldersUiState(
                isLoading = false,
                sources = listOf(SourceUi("tree://primary%3AMusic", "Music")),
                isScanning = true,
            ),
            onEvent = {},
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
