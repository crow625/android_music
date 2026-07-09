package com.example.androidmusic.ui.folders

import com.example.androidmusic.domain.diagnostics.FakeLogger
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakeSourceRepository
import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.usecase.AddSourceUseCase
import com.example.androidmusic.domain.usecase.GetFoldersUseCase
import com.example.androidmusic.domain.usecase.ObserveSourcesUseCase
import com.example.androidmusic.domain.usecase.RemoveSourceUseCase
import com.example.androidmusic.domain.usecase.ScanLibraryUseCase
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

    private fun viewModel(
        files: FakeAudioFileRepository,
        sources: FakeSourceRepository = FakeSourceRepository(),
    ) = FoldersViewModel(
        getFolders = GetFoldersUseCase(files),
        observeSources = ObserveSourcesUseCase(sources),
        addSource = AddSourceUseCase(sources),
        removeSource = RemoveSourceUseCase(sources),
        scanLibrary = ScanLibraryUseCase(sources, files),
        logger = FakeLogger(),
    )

    @Test
    fun `emits browse folders with derived names and counts`() = runTest(mainDispatcherRule.testDispatcher) {
        val files = FakeAudioFileRepository(
            library = Library(
                listOf(
                    audioFile(id = "1", parentFolderUri = "content://tree/primary%3AMusic%2FRock"),
                    audioFile(id = "2", parentFolderUri = "content://tree/primary%3AMusic%2FRock"),
                    audioFile(id = "3", parentFolderUri = "content://tree/primary%3AMusic%2FJazz"),
                ),
            ),
        )
        val viewModel = viewModel(files)
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val folders = viewModel.uiState.value.folders
        assertEquals(listOf("Jazz", "Rock"), folders.map { it.name })
        assertEquals(2, folders.first { it.name == "Rock" }.trackCount)
        collector.cancel()
    }

    @Test
    fun `picking a folder adds the source and records a scan summary`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val files = FakeAudioFileRepository(
                scanReport = ScanReport(
                    indexed = 3,
                    skippedUnsupported = 1,
                    metadataFailed = 0,
                    unreadable = 0,
                    problems = emptyList(),
                ),
            )
            val viewModel = viewModel(files)
            val collector = launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onFolderPicked(MediaUri("tree://a"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf("tree://a"), state.sources.map { it.uri })
            assertEquals(3, state.lastScan?.indexed)
            assertEquals(listOf(MediaUri("tree://a")), files.lastScannedSources)
            collector.cancel()
        }

    @Test
    fun `removing a source drops it from the sources list`() = runTest(mainDispatcherRule.testDispatcher) {
        val files = FakeAudioFileRepository()
        val viewModel = viewModel(files)
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onFolderPicked(MediaUri("tree://a"))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.sources.size)

        viewModel.onRemoveSource("tree://a")
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.sources.size)
        collector.cancel()
    }
}
