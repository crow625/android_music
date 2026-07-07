package com.example.androidmusic.ui.playlists

import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.testing.FakePlaylistRepository
import com.example.androidmusic.domain.usecase.GetPlaylistsUseCase
import com.example.androidmusic.domain.usecase.PlaylistCommands
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlaylistsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits existing playlists then reflects a create`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakePlaylistRepository(initial = listOf(Playlist(1, "Focus", emptyList())))
        val viewModel = PlaylistsViewModel(GetPlaylistsUseCase(repo), PlaylistCommands(repo))
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(listOf("Focus"), viewModel.uiState.value.playlists.map { it.name })

        viewModel.onCreatePlaylist("Roadtrip")
        advanceUntilIdle()
        assertEquals(listOf("Focus", "Roadtrip"), viewModel.uiState.value.playlists.map { it.name })
        collector.cancel()
    }

    @Test
    fun `blank names are ignored`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakePlaylistRepository()
        val viewModel = PlaylistsViewModel(GetPlaylistsUseCase(repo), PlaylistCommands(repo))
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onCreatePlaylist("   ")
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.playlists.size)
        collector.cancel()
    }
}
