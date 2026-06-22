package com.example.androidmusic.domain.model

/** A grouping of tracks that share a performing artist. */
data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
)
