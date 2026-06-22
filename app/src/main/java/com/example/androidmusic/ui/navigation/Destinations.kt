package com.example.androidmusic.ui.navigation

import android.net.Uri

/** Navigation routes. */
object Destinations {
    const val LIBRARY = "library"
    const val ALBUMS = "albums"
    const val ARTISTS = "artists"
    const val PLAYLISTS = "playlists"
    const val SOURCES = "sources"
    const val NOW_PLAYING = "now-playing"

    const val ALBUM_DETAIL = "albums/{albumId}"
    const val ARTIST_DETAIL = "artists/{artistId}"

    const val ALBUM_ID_ARG = "albumId"
    const val ARTIST_ID_ARG = "artistId"

    fun albumDetail(albumId: String): String = "albums/${Uri.encode(albumId)}"
    fun artistDetail(artistId: String): String = "artists/${Uri.encode(artistId)}"
}
