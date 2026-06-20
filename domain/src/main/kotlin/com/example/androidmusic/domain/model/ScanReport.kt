package com.example.androidmusic.domain.model

/** Outcome of a library scan, so the user can be told why a file isn't listed. */
data class ScanReport(
    val indexed: Int,
    val skippedUnsupported: Int,
    val metadataFailed: Int,
    val unreadable: Int,
    val problems: List<ScanProblem>,
) {
    companion object {
        val EMPTY = ScanReport(0, 0, 0, 0, emptyList())
    }
}

data class ScanProblem(val uri: MediaUri, val reason: String)
