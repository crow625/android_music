package com.example.androidmusic.domain.playback

/** What "skip previous" should do given how far into the current track we are. */
enum class PreviousAction { RestartCurrent, GoToPrevious }

object PlaybackDecisions {

    const val PREVIOUS_RESTART_THRESHOLD_MS = 3_000L

    /**
     * If more than [thresholdMs] into the current track, "previous" restarts the
     * current track; otherwise it moves to the previous track (feature-spec §5.2).
     */
    fun previousAction(
        positionMs: Long,
        thresholdMs: Long = PREVIOUS_RESTART_THRESHOLD_MS,
    ): PreviousAction =
        if (positionMs > thresholdMs) PreviousAction.RestartCurrent else PreviousAction.GoToPrevious
}
