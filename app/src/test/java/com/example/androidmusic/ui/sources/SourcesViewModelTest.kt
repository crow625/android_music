package com.example.androidmusic.ui.sources

import app.cash.turbine.test
import com.example.androidmusic.domain.diagnostics.FakeLogger
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakeSourceRepository
import com.example.androidmusic.domain.usecase.AddSourceUseCase
import com.example.androidmusic.domain.usecase.ObserveSourcesUseCase
import com.example.androidmusic.domain.usecase.RemoveSourceUseCase
import com.example.androidmusic.domain.usecase.ScanLibraryUseCase
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SourcesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        sources: FakeSourceRepository,
        files: FakeAudioFileRepository,
    ) = SourcesViewModel(
        observeSources = ObserveSourcesUseCase(sources),
        addSource = AddSourceUseCase(sources),
        removeSource = RemoveSourceUseCase(sources),
        scanLibrary = ScanLibraryUseCase(sources, files),
        logger = FakeLogger(),
    )

    @Test
    fun `picking a folder adds the source and records a scan summary`() = runTest {
        val sources = FakeSourceRepository()
        val files = FakeAudioFileRepository(
            scanReport = ScanReport(
                indexed = 3,
                skippedUnsupported = 1,
                metadataFailed = 0,
                unreadable = 0,
                problems = emptyList(),
            ),
        )
        val vm = viewModel(sources, files)

        vm.uiState.test {
            awaitItem() // initial empty state
            vm.onFolderPicked(MediaUri("tree://a"))

            var state = awaitItem()
            while (state.sources.isEmpty() || state.lastScan == null) {
                state = awaitItem()
            }

            assertEquals(listOf("tree://a"), state.sources.map { it.uri })
            assertEquals(3, state.lastScan?.indexed)
            assertEquals(listOf(MediaUri("tree://a")), files.lastScannedSources)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing a source drops it from the list`() = runTest {
        val sources = FakeSourceRepository()
        val files = FakeAudioFileRepository()
        val vm = viewModel(sources, files)

        vm.onFolderPicked(MediaUri("tree://a"))

        vm.uiState.test {
            var state = awaitItem()
            while (state.sources.isEmpty()) state = awaitItem()
            assertEquals(1, state.sources.size)

            vm.onRemoveSource("tree://a")
            while (state.sources.isNotEmpty()) state = awaitItem()
            assertEquals(0, state.sources.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
