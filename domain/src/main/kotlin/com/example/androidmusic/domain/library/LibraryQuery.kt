package com.example.androidmusic.domain.library

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.SortOrder

/** Pure search-filter + sort for the All-Tracks library view. */
object LibraryQuery {

    /** Filters by a normalized substring match on title/artist/album, then sorts. */
    fun apply(tracks: List<AudioFile>, query: String, sort: SortOrder): List<AudioFile> =
        tracks.filter { it.matches(query) }.sortedWith(comparator(sort))

    fun comparator(sort: SortOrder): Comparator<AudioFile> = when (sort) {
        SortOrder.Title -> compareBy { Normalize.key(it.title) }
        SortOrder.Artist -> compareBy(
            { Normalize.key(it.artist) },
            { Normalize.key(it.album) },
            { it.discNumber },
            { it.trackNumber },
        )
        SortOrder.Album -> compareBy(
            { Normalize.key(it.album) },
            { it.discNumber },
            { it.trackNumber },
        )
    }

    private fun AudioFile.matches(query: String): Boolean {
        if (query.isBlank()) return true
        val q = Normalize.key(query)
        return Normalize.key(title).contains(q) ||
            Normalize.key(artist).contains(q) ||
            Normalize.key(album).contains(q)
    }
}
