package com.example.androidmusic.domain.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusPolicyTest {

    @Test
    fun `transient loss pauses then gain resumes`() {
        val (afterLoss, lossAction) = FocusPolicy.reduce(FocusState(), FocusEvent.LossTransient)
        assertEquals(FocusAction.PauseTransient, lossAction)
        assertTrue(afterLoss.pausedForTransientLoss)

        val (afterGain, gainAction) = FocusPolicy.reduce(afterLoss, FocusEvent.Gain)
        assertEquals(FocusAction.Resume, gainAction)
        assertFalse(afterGain.pausedForTransientLoss)
    }

    @Test
    fun `can-duck loss ducks then gain restores volume`() {
        val (afterDuck, duckAction) = FocusPolicy.reduce(FocusState(), FocusEvent.LossTransientCanDuck)
        assertEquals(FocusAction.Duck, duckAction)
        assertTrue(afterDuck.ducked)

        val (afterGain, gainAction) = FocusPolicy.reduce(afterDuck, FocusEvent.Gain)
        assertEquals(FocusAction.RestoreVolume, gainAction)
        assertFalse(afterGain.ducked)
    }

    @Test
    fun `permanent loss pauses and does not resume on a later gain`() {
        val (afterLoss, lossAction) = FocusPolicy.reduce(
            FocusState(pausedForTransientLoss = true),
            FocusEvent.Loss,
        )
        assertEquals(FocusAction.Pause, lossAction)
        assertFalse(afterLoss.pausedForTransientLoss)

        val (_, gainAction) = FocusPolicy.reduce(afterLoss, FocusEvent.Gain)
        assertEquals(FocusAction.None, gainAction)
    }

    @Test
    fun `gain with no prior loss does nothing`() {
        val (_, action) = FocusPolicy.reduce(FocusState(), FocusEvent.Gain)
        assertEquals(FocusAction.None, action)
    }

    @Test
    fun `restoring volume takes priority over resuming`() {
        val state = FocusState(pausedForTransientLoss = true, ducked = true)
        val (_, action) = FocusPolicy.reduce(state, FocusEvent.Gain)
        assertEquals(FocusAction.RestoreVolume, action)
    }
}
