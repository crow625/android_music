package com.example.androidmusic.ui.library

import app.cash.turbine.test
import com.example.androidmusic.domain.diagnostics.FakeLogger
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakePlaybackController
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

    private val controller = FakePlaybackController()

    private fun viewModel(repository: FakeAudioFileRepository) =
        LibraryViewModel(ObserveLibraryUseCase(repository), controller, FakeLogger())

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

    @Test
    fun `tapping a track plays it with the full library as context`() = runTest {
        val tracks = listOf(audioFile(id = "1"), audioFile(id = "2"), audioFile(id = "3"))
        val vm = viewModel(FakeAudioFileRepository(library = Library(tracks)))

        // Collect once so the library list is captured by the ViewModel.
        vm.uiState.test {
            awaitItem().let { if (it.isLoading) awaitItem() else it }
            vm.onTrackClicked("2")
            cancelAndIgnoreRemainingEvents()
        }

        val source = controller.lastSource
        assertTrue(source is QueueSource.FromSingleTrack)
        source as QueueSource.FromSingleTrack
        assertEquals("2", source.file.id)
        assertEquals(listOf("1", "2", "3"), source.contextList.map { it.id })
    }
}
