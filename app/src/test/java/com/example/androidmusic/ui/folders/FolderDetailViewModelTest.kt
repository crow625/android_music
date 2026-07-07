package com.example.androidmusic.ui.folders

import androidx.lifecycle.SavedStateHandle
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakePlaybackController
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.GetFolderTracksUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FolderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()

    @Test
    fun `loads folder tracks and plays the folder`() = runTest(mainDispatcherRule.testDispatcher) {
        val folder = "content://tree/primary%3AMusic%2FRock"
        val tracks = listOf(
            audioFile(id = "a", parentFolderUri = folder, trackNumber = 1),
            audioFile(id = "b", parentFolderUri = folder, trackNumber = 2),
            audioFile(id = "c", parentFolderUri = "content://tree/other"),
        )
        val repo = FakeAudioFileRepository(library = Library(tracks))
        val viewModel = FolderDetailViewModel(
            SavedStateHandle(mapOf("folderUri" to folder)),
            GetFolderTracksUseCase(repo),
            controller,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Rock", state.name)
        assertEquals(listOf("a", "b"), state.tracks.map { it.id })

        viewModel.onPlayFolder()
        assertEquals(QueueSource.FromFolder(MediaUri(folder)), controller.lastSource)
        collector.cancel()
    }
}
