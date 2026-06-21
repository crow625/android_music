package com.example.androidmusic.ui.player

/** Events emitted by [NowPlayingScreen]. */
sealed interface NowPlayingEvent {
    data object PlayPause : NowPlayingEvent
    data object Next : NowPlayingEvent
    data object Previous : NowPlayingEvent
    data class Seek(val positionMs: Long) : NowPlayingEvent
    data object ToggleShuffle : NowPlayingEvent
    data object CycleRepeat : NowPlayingEvent
    data object ToggleQueue : NowPlayingEvent
    data class JumpTo(val index: Int) : NowPlayingEvent
    data class Remove(val index: Int) : NowPlayingEvent
    data object ClearQueue : NowPlayingEvent
}
