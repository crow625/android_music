package com.example.androidmusic.domain.model

/** A grouping of tracks that share an album (and album artist). */
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val artworkUri: String? = null,
)
