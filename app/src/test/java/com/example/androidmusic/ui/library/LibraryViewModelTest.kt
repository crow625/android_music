package com.example.androidmusic.ui.library

import app.cash.turbine.test
import com.example.androidmusic.domain.diagnostics.FakeLogger
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.ObserveLibraryUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeAudioFileRepository) =
        LibraryViewModel(ObserveLibraryUseCase(repository), FakeLogger())

    @Test
    fun `emits loaded tracks from the library`() = runTest {
        val repository = FakeAudioFileRepository(
            library = Library(listOf(audioFile(id = "1", title = "Song", artist = "A", album = "Al"))),
        )

        viewModel(repository).uiState.test {
            val settled = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertEquals(false, settled.isLoading)
            assertEquals(listOf("1"), settled.tracks.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty library yields the empty state`() = runTest {
        viewModel(FakeAudioFileRepository(library = Library(emptyList()))).uiState.test {
            val settled = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertTrue(settled.isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
