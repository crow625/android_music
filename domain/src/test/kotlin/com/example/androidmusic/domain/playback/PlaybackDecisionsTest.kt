package com.example.androidmusic.domain.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackDecisionsTest {

    @Test
    fun `more than three seconds in restarts the current track`() {
        assertEquals(
            PreviousAction.RestartCurrent,
            PlaybackDecisions.previousAction(positionMs = 3_001L),
        )
    }

    @Test
    fun `at or below three seconds goes to the previous track`() {
        assertEquals(
            PreviousAction.GoToPrevious,
            PlaybackDecisions.previousAction(positionMs = 3_000L),
        )
        assertEquals(
            PreviousAction.GoToPrevious,
            PlaybackDecisions.previousAction(positionMs = 0L),
        )
    }
}
