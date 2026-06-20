package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.library.LibraryScreen
import com.example.androidmusic.ui.library.LibraryUiState
import com.example.androidmusic.ui.library.TrackUi
import com.example.androidmusic.ui.theme.AndroidMusicTheme

@Preview(name = "Library – loaded", showBackground = true)
@Composable
fun LibraryLoadedPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        LibraryScreen(
            state = LibraryUiState(
                isLoading = false,
                tracks = listOf(
                    TrackUi("1", "Aerials", "System of a Down", "Toxicity"),
                    TrackUi("2", "Chop Suey!", "System of a Down", "Toxicity"),
                    TrackUi("3", "Teardrop", "Massive Attack", "Mezzanine"),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Library – empty", showBackground = true)
@Composable
fun LibraryEmptyPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        LibraryScreen(state = LibraryUiState(isLoading = false), onEvent = {})
    }
}

@Preview(name = "Library – loading", showBackground = true)
@Composable
fun LibraryLoadingPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        LibraryScreen(state = LibraryUiState(isLoading = true), onEvent = {})
    }
}
