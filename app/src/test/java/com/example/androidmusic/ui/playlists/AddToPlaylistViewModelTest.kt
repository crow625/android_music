package com.example.androidmusic.ui.playlists

import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakePlaylistRepository
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.GetPlaylistsUseCase
import com.example.androidmusic.domain.usecase.ObserveLibraryUseCase
import com.example.androidmusic.domain.usecase.PlaylistCommands
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AddToPlaylistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val files = FakeAudioFileRepository(
        library = Library(listOf(audioFile(id = "a", title = "Teardrop"))),
    )

    private fun viewModel(repo: FakePlaylistRepository) = AddToPlaylistViewModel(
        ObserveLibraryUseCase(files),
        GetPlaylistsUseCase(repo),
        PlaylistCommands(repo),
    )

    @Test
    fun `adds a library track to an existing playlist`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakePlaylistRepository()
        val playlistId = repo.createPlaylist("Focus").id
        val vm = viewModel(repo)

        vm.onAddToPlaylist(trackId = "a", playlistId = playlistId)
        advanceUntilIdle()

        val entries = repo.playlists.single { it.id == playlistId }.entries
        assertEquals(listOf("a"), entries.map { it.trackId })
    }

    @Test
    fun `create-and-add makes a new playlist containing the track`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakePlaylistRepository()
            val vm = viewModel(repo)

            vm.onCreateAndAdd(trackId = "a", name = "New Mix")
            advanceUntilIdle()

            val created = repo.playlists.single { it.name == "New Mix" }
            assertEquals(listOf("a"), created.entries.map { it.trackId })
        }

    @Test
    fun `unknown track id is a no-op`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakePlaylistRepository()
        val playlistId = repo.createPlaylist("Focus").id
        val vm = viewModel(repo)

        vm.onAddToPlaylist(trackId = "missing", playlistId = playlistId)
        advanceUntilIdle()

        assertEquals(0, repo.playlists.single { it.id == playlistId }.entries.size)
    }
}
