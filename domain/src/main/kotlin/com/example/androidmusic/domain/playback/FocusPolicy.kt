package com.example.androidmusic.domain.playback

/** Audio-focus changes the system reports (mapped from `AudioManager` by the player layer). */
enum class FocusEvent { Gain, Loss, LossTransient, LossTransientCanDuck }

/** What the player should do in response to a focus change. */
sealed interface FocusAction {
    /** Permanent loss: pause and do not auto-resume. */
    data object Pause : FocusAction

    /** Transient loss: pause, but resume when focus is regained. */
    data object PauseTransient : FocusAction

    data object Resume : FocusAction
    data object Duck : FocusAction
    data object RestoreVolume : FocusAction
    data object None : FocusAction
}

/** State the policy carries between focus events. */
data class FocusState(
    val pausedForTransientLoss: Boolean = false,
    val ducked: Boolean = false,
)

/**
 * Pure audio-focus policy (technical-spec §6.3). The player layer owns the
 * `AudioManager` plumbing; this decides the behaviour and is fully unit-tested.
 */
object FocusPolicy {

    fun reduce(state: FocusState, event: FocusEvent): Pair<FocusState, FocusAction> = when (event) {
        // Permanent loss: pause, abandon focus, forget any pending resume.
        FocusEvent.Loss -> FocusState() to FocusAction.Pause

        FocusEvent.LossTransient ->
            state.copy(pausedForTransientLoss = true, ducked = false) to FocusAction.PauseTransient

        FocusEvent.LossTransientCanDuck ->
            state.copy(ducked = true) to FocusAction.Duck

        FocusEvent.Gain -> when {
            state.ducked -> state.copy(ducked = false) to FocusAction.RestoreVolume
            state.pausedForTransientLoss -> state.copy(pausedForTransientLoss = false) to FocusAction.Resume
            else -> state to FocusAction.None
        }
    }
}
