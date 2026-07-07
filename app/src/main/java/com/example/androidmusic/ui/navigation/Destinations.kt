package com.example.androidmusic.ui.navigation

import android.net.Uri

/** Navigation routes. */
object Destinations {
    const val LIBRARY = "library"
    const val ALBUMS = "albums"
    const val ARTISTS = "artists"
    const val FOLDERS = "folders"
    const val PLAYLISTS = "playlists"
    const val SOURCES = "sources"
    const val NOW_PLAYING = "now-playing"

    const val ALBUM_DETAIL = "albums/{albumId}"
    const val ARTIST_DETAIL = "artists/{artistId}"
    const val FOLDER_DETAIL = "folders/{folderUri}"
    const val PLAYLIST_DETAIL = "playlists/{playlistId}"

    const val ALBUM_ID_ARG = "albumId"
    const val ARTIST_ID_ARG = "artistId"
    const val FOLDER_URI_ARG = "folderUri"
    const val PLAYLIST_ID_ARG = "playlistId"

    fun albumDetail(albumId: String): String = "albums/${Uri.encode(albumId)}"
    fun artistDetail(artistId: String): String = "artists/${Uri.encode(artistId)}"
    fun folderDetail(folderUri: String): String = "folders/${Uri.encode(folderUri)}"
    fun playlistDetail(playlistId: Long): String = "playlists/$playlistId"
}
