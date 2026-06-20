package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.sources.ScanSummaryUi
import com.example.androidmusic.ui.sources.SourceUi
import com.example.androidmusic.ui.sources.SourcesScreen
import com.example.androidmusic.ui.sources.SourcesUiState
import com.example.androidmusic.ui.theme.AndroidMusicTheme

@Preview(name = "Sources – with folders", showBackground = true)
@Composable
fun SourcesPopulatedPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        SourcesScreen(
            state = SourcesUiState(
                sources = listOf(
                    SourceUi("tree://primary%3AMusic", "Music"),
                    SourceUi("tree://sdcard%3AAlbums", "Albums"),
                ),
                lastScan = ScanSummaryUi(indexed = 128, skipped = 3, unreadable = 1, problems = 1),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Sources – empty", showBackground = true)
@Composable
fun SourcesEmptyPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        SourcesScreen(state = SourcesUiState(), onEvent = {})
    }
}

@Preview(name = "Sources – scanning", showBackground = true)
@Composable
fun SourcesScanningPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        SourcesScreen(
            state = SourcesUiState(
                sources = listOf(SourceUi("tree://primary%3AMusic", "Music")),
                isScanning = true,
            ),
            onEvent = {},
        )
    }
}
