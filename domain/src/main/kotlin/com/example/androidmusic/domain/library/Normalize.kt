package com.example.androidmusic.domain.library

/** Metadata normalization used for stable IDs and playlist resolution matching. */
object Normalize {
    /** Locale-invariant, trimmed, lower-cased key for case-insensitive matching. */
    fun key(value: String): String = value.trim().lowercase()
}
