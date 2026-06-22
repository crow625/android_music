package com.example.androidmusic.ui.artists

import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.GetArtistsUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ArtistsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits artists with counts`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeAudioFileRepository(
            library = Library(
                listOf(
                    audioFile(id = "1", artist = "X", album = "One"),
                    audioFile(id = "2", artist = "X", album = "Two"),
                    audioFile(id = "3", artist = "Y", album = "Three"),
                ),
            ),
        )
        val viewModel = ArtistsViewModel(GetArtistsUseCase(repo))
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("X", "Y"), state.artists.map { it.name })
        assertEquals(2, state.artists.first { it.name == "X" }.albumCount)
        collector.cancel()
    }
}
