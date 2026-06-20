package com.example.androidmusic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.diagnostics.AppError
import com.example.androidmusic.domain.diagnostics.DiagnosticReporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Phase 0 ViewModel. It does little beyond proving the wiring end-to-end:
 * UI → ViewModel → the [DiagnosticReporter] domain seam → Room.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    diagnosticReporter: DiagnosticReporter,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        diagnosticReporter.observeRecent()
            .map { errors -> errors.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = INITIAL,
            )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        val INITIAL = HomeUiState(
            title = "Android Music",
            message = "Phase 0 scaffold",
        )
    }
}

private fun List<AppError>.toUiState() = HomeUiState(
    title = "Android Music",
    message = "Phase 0 scaffold — $size diagnostic event(s) recorded",
)
