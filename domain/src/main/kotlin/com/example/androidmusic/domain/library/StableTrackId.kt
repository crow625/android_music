package com.example.androidmusic.domain.library

import java.security.MessageDigest

/**
 * Computes the stable track identity (technical-spec §7.2):
 *
 * 1. The embedded MusicBrainz Recording ID if present — globally unique, read
 *    locally, and cleanly distinguishes remasters/versions.
 * 2. Otherwise a deterministic SHA-1 of normalized title+artist+album plus a
 *    duration bucket (rounded to the nearest 2s) to absorb re-encode jitter while
 *    separating obviously different recordings.
 *
 * Known residual: two *untagged* genuinely-different recordings sharing
 * title/artist/album/duration still collide; documented and accepted for v1.
 */
object StableTrackId {

    const val DURATION_BUCKET_MS = 2_000L
    private const val SEPARATOR = "§" // §

    fun compute(
        title: String,
        artist: String,
        album: String,
        durationMs: Long,
        musicBrainzRecordingId: String? = null,
    ): String {
        val mbId = musicBrainzRecordingId?.trim()
        if (!mbId.isNullOrEmpty()) return mbId

        val raw = listOf(
            Normalize.key(title),
            Normalize.key(artist),
            Normalize.key(album),
            bucketDuration(durationMs).toString(),
        ).joinToString(SEPARATOR)
        return sha1Hex(raw)
    }

    /** Rounds a duration to the nearest [DURATION_BUCKET_MS]. */
    fun bucketDuration(durationMs: Long): Long {
        if (durationMs <= 0L) return 0L
        return ((durationMs + DURATION_BUCKET_MS / 2) / DURATION_BUCKET_MS) * DURATION_BUCKET_MS
    }

    private fun sha1Hex(input: String): String =
        MessageDigest.getInstance("SHA-1")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}
