package com.example.androidmusic.ui.player

import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackProgress
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.RepeatMode
import com.example.androidmusic.domain.testing.FakePlaybackController
import com.example.androidmusic.domain.testing.audioFiles
import com.example.androidmusic.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()

    @Test
    fun `reflects the current track and playing state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = PlayerViewModel(controller)
        val collector = launch { viewModel.uiState.collect {} }
        controller.setQueue(PlayQueue(items = audioFiles(3), currentIndex = 1))
        controller.setState(PlaybackState.Playing(index = 1, positionMs = 0))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("t2", state.currentTrack?.id)
        assertTrue(state.isPlaying)
        collector.cancel()
    }

    @Test
    fun `play-pause pauses when playing`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = PlayerViewModel(controller)
        val collector = launch { viewModel.uiState.collect {} }
        controller.setQueue(PlayQueue(items = audioFiles(1), currentIndex = 0))
        controller.setState(PlaybackState.Playing(index = 0, positionMs = 0))
        advanceUntilIdle()

        viewModel.onPlayPause()

        assertEquals(1, controller.pauseCalls)
        assertEquals(0, controller.playCalls)
        collector.cancel()
    }

    @Test
    fun `no track yields an empty bar`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = PlayerViewModel(controller)
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasTrack)
        collector.cancel()
    }

    @Test
    fun `reflects progress, shuffle and repeat`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = PlayerViewModel(controller)
        val collector = launch { viewModel.uiState.collect {} }
        controller.setQueue(PlayQueue(items = audioFiles(3), currentIndex = 0))
        controller.setState(PlaybackState.Playing(index = 0, positionMs = 0))
        controller.setProgress(PlaybackProgress(positionMs = 65_000, durationMs = 210_000))
        viewModel.onToggleShuffle()
        viewModel.onCycleRepeat()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(65_000L, state.positionMs)
        assertEquals(210_000L, state.durationMs)
        assertEquals(3, state.queue.size)
        assertTrue(state.isShuffleOn)
        assertEquals(RepeatMode.RepeatQueue, state.repeatMode)
        collector.cancel()
    }

    @Test
    fun `seek, jump, remove and clear delegate to the controller`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = PlayerViewModel(controller)

        viewModel.onSeek(1_234)
        viewModel.onJumpTo(2)
        viewModel.onRemoveFromQueue(1)
        viewModel.onClearQueue()

        assertEquals(1_234L, controller.lastSeekMs)
        assertEquals(2, controller.jumpedToIndex)
        assertEquals(1, controller.removedIndex)
        assertEquals(1, controller.clearCalls)
    }
}
