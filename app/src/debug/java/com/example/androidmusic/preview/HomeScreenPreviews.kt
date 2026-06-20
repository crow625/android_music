package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.home.HomeScreen
import com.example.androidmusic.ui.home.HomeUiState
import com.example.androidmusic.ui.theme.AndroidMusicTheme

/**
 * Previews live in the dedicated `debug` source set, physically separated from
 * production composables (which never import fake/preview code). Each `@Preview`
 * doubles as a Roborazzi golden via the screenshot test, so one definition serves
 * both the IDE/demo reference and regression testing.
 *
 * `dynamicColor = false` keeps previews deterministic (no device wallpaper-derived
 * Material You palette), which matters for stable goldens.
 */
@Preview(name = "Home – default", showBackground = true)
@Composable
fun HomeScreenDefaultPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        HomeScreen(
            state = HomeUiState(
                title = "Android Music",
                message = "Phase 0 scaffold",
            ),
        )
    }
}

@Preview(name = "Home – with diagnostics", showBackground = true)
@Composable
fun HomeScreenWithDiagnosticsPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        HomeScreen(
            state = HomeUiState(
                title = "Android Music",
                message = "Phase 0 scaffold — 3 diagnostic event(s) recorded",
            ),
        )
    }
}
