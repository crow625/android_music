package com.example.androidmusic.domain.concurrency

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Seam over coroutine dispatchers. Inject this instead of referencing
 * `Dispatchers.IO`/`Default` directly so tests can substitute a test dispatcher.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
