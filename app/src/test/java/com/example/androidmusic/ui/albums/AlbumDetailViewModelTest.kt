package com.example.androidmusic.ui.albums

import androidx.lifecycle.SavedStateHandle
import com.example.androidmusic.domain.library.LibraryGroupings
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakePlaybackController
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.GetAlbumTracksUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()

    @Test
    fun `loads album tracks in order and plays the album`() = runTest(mainDispatcherRule.testDispatcher) {
        val tracks = listOf(
            audioFile(id = "a", album = "Toxicity", artist = "SOAD", trackNumber = 1),
            audioFile(id = "b", album = "Toxicity", artist = "SOAD", trackNumber = 2),
        )
        val repo = FakeAudioFileRepository(library = Library(tracks))
        val albumId = LibraryGroupings.albumKey(tracks.first())
        val viewModel = AlbumDetailViewModel(
            SavedStateHandle(mapOf("albumId" to albumId)),
            GetAlbumTracksUseCase(repo),
            controller,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Toxicity", state.title)
        assertEquals(listOf("a", "b"), state.tracks.map { it.id })

        viewModel.onPlayAlbum()
        assertEquals(QueueSource.FromAlbum(albumId), controller.lastSource)
        collector.cancel()
    }
}
