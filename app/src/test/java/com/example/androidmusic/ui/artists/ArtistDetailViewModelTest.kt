package com.example.androidmusic.ui.artists

import androidx.lifecycle.SavedStateHandle
import com.example.androidmusic.domain.library.LibraryGroupings
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakePlaybackController
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.GetArtistAlbumsUseCase
import com.example.androidmusic.domain.usecase.GetArtistTracksUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ArtistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()

    @Test
    fun `loads artist albums and tracks and plays the artist`() = runTest(mainDispatcherRule.testDispatcher) {
        val tracks = listOf(
            audioFile(id = "a", artist = "SOAD", album = "Toxicity"),
            audioFile(id = "b", artist = "SOAD", album = "Steal This Album"),
        )
        val repo = FakeAudioFileRepository(library = Library(tracks))
        val artistId = LibraryGroupings.artistKey(tracks.first())
        val viewModel = ArtistDetailViewModel(
            SavedStateHandle(mapOf("artistId" to artistId)),
            GetArtistAlbumsUseCase(repo),
            GetArtistTracksUseCase(repo),
            controller,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("SOAD", state.name)
        assertEquals(2, state.albums.size)
        assertEquals(2, state.tracks.size)

        viewModel.onPlayArtist()
        assertEquals(QueueSource.FromArtist(artistId), controller.lastSource)
        collector.cancel()
    }
}
