package com.example.androidmusic.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.error
import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackProgress
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.model.RepeatMode
import com.example.androidmusic.domain.player.ConnectionState
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.BuildQueueUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Client-side [PlaybackController] backed by a Media3 [MediaController] that
 * connects to [MusicPlaybackService]. Shuffle is resolved in the domain
 * [PlayQueue]; repeat is delegated to the player's repeat mode.
 */
@Singleton
class MediaControllerPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val buildQueue: BuildQueueUseCase,
    private val appScope: CoroutineScope,
    private val logger: Logger,
) : PlaybackController {

    private val _connection = MutableStateFlow(ConnectionState.Connecting)
    override val connection: StateFlow<ConnectionState> = _connection

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state

    private val _queue = MutableStateFlow(PlayQueue.EMPTY)
    override val queue: StateFlow<PlayQueue> = _queue

    private val _progress = MutableStateFlow(PlaybackProgress())
    override val progress: StateFlow<PlaybackProgress> = _progress

    private val _repeatMode = MutableStateFlow(RepeatMode.Off)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private var controller: MediaController? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = refreshState(player)
    }

    init {
        connect()
        startProgressTicker()
    }

    private fun startProgressTicker() {
        appScope.launch {
            while (true) {
                delay(PROGRESS_TICK_MS)
                val mediaController = controller ?: continue
                _progress.value = PlaybackProgress(
                    positionMs = mediaController.currentPosition.coerceAtLeast(0),
                    durationMs = mediaController.duration.coerceAtLeast(0),
                )
            }
        }
    }

    private fun connect() {
        val token = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            runCatching { future.get() }
                .onSuccess { mediaController ->
                    controller = mediaController.also {
                        it.addListener(listener)
                        refreshState(it)
                    }
                    _connection.value = ConnectionState.Connected
                }
                .onFailure { throwable ->
                    logger.error(TAG, "Failed to connect to playback service", throwable)
                    _connection.value = ConnectionState.Disconnected
                }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun playSource(source: QueueSource) {
        appScope.launch {
            val built = buildQueue(source)
            _queue.value = built
            val mediaController = controller ?: return@launch
            mediaController.setMediaItems(built.items.map { it.toMediaItem() }, built.currentIndex, 0L)
            mediaController.prepare()
            mediaController.play()
        }
    }

    override fun play() {
        controller?.play()
    }

    override fun pause() {
        controller?.pause()
    }

    override fun skipToNext() {
        controller?.seekToNextMediaItem()
    }

    override fun skipToPrevious() {
        // Player.seekToPrevious() restarts the current track if past the
        // maxSeekToPreviousPosition (3s by default), else goes to the previous.
        controller?.seekToPrevious()
    }

    override fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    override fun toggleShuffle() {
        val updated =
            if (_queue.value.shuffled) _queue.value.unshuffle() else _queue.value.shuffle(Random.Default)
        _queue.value = updated
        val player = controller ?: return
        // Reorder *around* the currently-playing item using incremental timeline
        // mutations so its MediaSource is never rebuilt — avoids a re-buffer stall.
        // (A full setMediaItems() would re-prepare and briefly pause the track.)
        val playing = player.currentMediaItemIndex
        if (player.mediaItemCount > playing + 1) {
            player.removeMediaItems(playing + 1, player.mediaItemCount)
        }
        if (playing > 0) {
            player.removeMediaItems(0, playing)
        }
        val index = updated.currentIndex
        val before = updated.items.subList(0, index).map { it.toMediaItem() }
        val after = updated.items.subList(index + 1, updated.items.size).map { it.toMediaItem() }
        if (before.isNotEmpty()) {
            player.addMediaItems(0, before)
        }
        if (after.isNotEmpty()) {
            player.addMediaItems(after)
        }
    }

    override fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.Off -> RepeatMode.RepeatQueue
            RepeatMode.RepeatQueue -> RepeatMode.RepeatOne
            RepeatMode.RepeatOne -> RepeatMode.Off
        }
        _repeatMode.value = next
        controller?.repeatMode = when (next) {
            RepeatMode.Off -> Player.REPEAT_MODE_OFF
            RepeatMode.RepeatQueue -> Player.REPEAT_MODE_ALL
            RepeatMode.RepeatOne -> Player.REPEAT_MODE_ONE
        }
    }

    override fun skipToQueueItem(index: Int) {
        controller?.seekTo(index, 0L)
    }

    override fun moveQueueItem(from: Int, to: Int) {
        val updated = _queue.value.moveItem(from, to)
        _queue.value = updated
        controller?.moveMediaItem(from, to)
    }

    override fun removeQueueItem(index: Int) {
        _queue.value = _queue.value.removeAt(index)
        controller?.removeMediaItem(index)
    }

    override fun clearQueue() {
        _queue.value = _queue.value.clearExceptCurrent()
        val mediaController = controller ?: return
        val current = mediaController.currentMediaItemIndex
        if (mediaController.mediaItemCount > current + 1) {
            mediaController.removeMediaItems(current + 1, mediaController.mediaItemCount)
        }
        if (current > 0) {
            mediaController.removeMediaItems(0, current)
        }
    }

    private fun refreshState(player: Player) {
        val index = player.currentMediaItemIndex
        val position = player.currentPosition
        _state.value = when {
            player.playerError != null ->
                PlaybackState.Error(index, player.playerError?.message ?: "Playback error")
            player.mediaItemCount == 0 || player.playbackState == Player.STATE_IDLE ->
                PlaybackState.Idle
            player.isPlaying -> PlaybackState.Playing(index, position)
            else -> PlaybackState.Paused(index, position)
        }
        // Keep the queue's current index in sync as playback advances.
        val current = _queue.value
        if (index in current.items.indices && index != current.currentIndex) {
            _queue.value = current.withCurrentIndex(index)
        }
    }

    private companion object {
        const val TAG = "PlaybackController"
        const val PROGRESS_TICK_MS = 500L
    }
}
