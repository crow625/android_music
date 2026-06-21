package com.example.androidmusic.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.androidmusic.domain.model.AudioFile

/** Maps a domain track to a Media3 [MediaItem] carrying display metadata. */
fun AudioFile.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri.value)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri?.value?.let(Uri::parse))
                .build(),
        )
        .build()
