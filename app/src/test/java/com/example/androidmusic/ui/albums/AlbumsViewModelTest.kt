package com.example.androidmusic.ui.albums

import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.GetAlbumsUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AlbumsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits albums sorted by title`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeAudioFileRepository(
            library = Library(
                listOf(
                    audioFile(id = "1", album = "Bravado", artist = "X"),
                    audioFile(id = "2", album = "Anthem", artist = "Y"),
                ),
            ),
        )
        val viewModel = AlbumsViewModel(GetAlbumsUseCase(repo))
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(listOf("Anthem", "Bravado"), state.albums.map { it.title })
        collector.cancel()
    }
}
