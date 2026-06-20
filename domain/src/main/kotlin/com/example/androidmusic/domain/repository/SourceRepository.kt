package com.example.androidmusic.domain.repository

import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.SourceFolder
import kotlinx.coroutines.flow.Flow

/** Manages the set of source folders the library is built from. */
interface SourceRepository {
    fun observeSources(): Flow<List<SourceFolder>>
    suspend fun addSource(uri: MediaUri)

    /** Removes the source and its tracks from the library (files on disk are untouched). */
    suspend fun removeSource(uri: MediaUri)
}
