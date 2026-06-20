package com.example.androidmusic.domain.model

/** The full indexed library of tracks. Album/artist groupings are derived views. */
data class Library(
    val tracks: List<AudioFile> = emptyList(),
)
