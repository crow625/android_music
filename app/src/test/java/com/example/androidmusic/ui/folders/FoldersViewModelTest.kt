package com.example.androidmusic.ui.folders

import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.GetFoldersUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FoldersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits folders with derived names and counts`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeAudioFileRepository(
            library = Library(
                listOf(
                    audioFile(id = "1", parentFolderUri = "content://tree/primary%3AMusic%2FRock"),
                    audioFile(id = "2", parentFolderUri = "content://tree/primary%3AMusic%2FRock"),
                    audioFile(id = "3", parentFolderUri = "content://tree/primary%3AMusic%2FJazz"),
                ),
            ),
        )
        val viewModel = FoldersViewModel(GetFoldersUseCase(repo))
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val folders = viewModel.uiState.value.folders
        assertEquals(listOf("Jazz", "Rock"), folders.map { it.name })
        assertEquals(2, folders.first { it.name == "Rock" }.trackCount)
        collector.cancel()
    }
}
