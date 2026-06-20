package com.example.androidmusic.domain.model

/**
 * A URI represented as a plain string so the domain layer never imports
 * `android.net.Uri`. The wrapper makes the intent of the value explicit.
 *
 * Conversion to/from `android.net.Uri` happens only in the `data/` and `player/`
 * layers, at the boundary of the pure-Kotlin domain.
 */
@JvmInline
value class MediaUri(val value: String)
