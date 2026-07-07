package com.example.androidmusic.domain.model

/** An on-disk folder that directly contains audio files. */
data class Folder(
    val uri: String,
    val trackCount: Int,
)
