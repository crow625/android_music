package com.example.androidmusic.ui.playlists

import androidx.lifecycle.SavedStateHandle
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakePlaybackController
import com.example.androidmusic.domain.testing.FakePlaylistRepository
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.ObservePlaylistUseCase
import com.example.androidmusic.domain.usecase.PlaylistCommands
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlaylistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()

    // Library has "a" and "b"; "gone" is intentionally absent so it stays unresolved.
    private val files = FakeAudioFileRepository(
        library = Library(
            listOf(
                audioFile(id = "a", title = "Teardrop", artist = "Massive Attack"),
                audioFile(id = "b", title = "Glory Box", artist = "Portishead"),
            ),
        ),
    )

    private suspend fun repoWithMix(): Pair<FakePlaylistRepository, Long> {
        val repo = FakePlaylistRepository()
        val id = repo.createPlaylist("Mix").id
        repo.addTrack(id, audioFile(id = "a", title = "Teardrop", artist = "Massive Attack"))
        repo.addTrack(id, audioFile(id = "gone", title = "Ghost", artist = "Nobody"))
        return repo to id
    }

    private fun viewModel(repo: FakePlaylistRepository, id: Long) = PlaylistDetailViewModel(
        SavedStateHandle(mapOf("playlistId" to id.toString())),
        ObservePlaylistUseCase(repo, files),
        PlaylistCommands(repo),
        controller,
    )

    @Test
    fun `resolves entries against the library and greys the missing`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val (repo, id) = repoWithMix()
            val vm = viewModel(repo, id)
            val collector = launch { vm.uiState.collect {} }
            advanceUntilIdle()

            val state = vm.uiState.value
            assertEquals("Mix", state.name)
            assertEquals(listOf("Teardrop", "Ghost"), state.entries.map { it.title })
            assertEquals(listOf(true, false), state.entries.map { it.isResolved })
            assertTrue(state.hasResolvableTracks)
            collector.cancel()
        }

    @Test
    fun `play uses the playlist source`() = runTest(mainDispatcherRule.testDispatcher) {
        val (repo, id) = repoWithMix()
        val vm = viewModel(repo, id)
        val collector = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onPlay()
        assertEquals(QueueSource.FromPlaylist(id), controller.lastSource)
        collector.cancel()
    }

    @Test
    fun `rename and remove are reflected in state`() = runTest(mainDispatcherRule.testDispatcher) {
        val (repo, id) = repoWithMix()
        val vm = viewModel(repo, id)
        val collector = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onRename("Evening")
        advanceUntilIdle()
        assertEquals("Evening", vm.uiState.value.name)

        val firstEntry = vm.uiState.value.entries.first().entryId
        vm.onRemoveEntry(firstEntry)
        advanceUntilIdle()
        assertEquals(listOf("Ghost"), vm.uiState.value.entries.map { it.title })
        collector.cancel()
    }

    @Test
    fun `delete flips exists to false`() = runTest(mainDispatcherRule.testDispatcher) {
        val (repo, id) = repoWithMix()
        val vm = viewModel(repo, id)
        val collector = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onDelete()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.exists)
        collector.cancel()
    }
}
