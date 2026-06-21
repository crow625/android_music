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
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.model.RepeatMode
import com.example.androidmusic.domain.player.ConnectionState
import com.example.androidmusic.domain.player.PlaybackController
import com.example.androidmusic.domain.usecase.BuildQueueUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
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

    private var controller: MediaController? = null
    private var repeatMode: RepeatMode = RepeatMode.Off

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = refreshState(player)
    }

    init {
        connect()
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
        val current = _queue.value
        val updated = if (current.shuffled) current.unshuffle() else current.shuffle(Random.Default)
        _queue.value = updated
        val mediaController = controller ?: return
        mediaController.setMediaItems(
            updated.items.map { it.toMediaItem() },
            updated.currentIndex,
            mediaController.currentPosition,
        )
        mediaController.prepare()
    }

    override fun cycleRepeatMode() {
        repeatMode = when (repeatMode) {
            RepeatMode.Off -> RepeatMode.RepeatQueue
            RepeatMode.RepeatQueue -> RepeatMode.RepeatOne
            RepeatMode.RepeatOne -> RepeatMode.Off
        }
        controller?.repeatMode = when (repeatMode) {
            RepeatMode.Off -> Player.REPEAT_MODE_OFF
            RepeatMode.RepeatQueue -> Player.REPEAT_MODE_ALL
            RepeatMode.RepeatOne -> Player.REPEAT_MODE_ONE
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
    }

    private companion object {
        const val TAG = "PlaybackController"
    }
}
