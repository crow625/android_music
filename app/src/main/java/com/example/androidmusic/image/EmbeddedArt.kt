package com.example.androidmusic.image

import android.media.MediaMetadataRetriever
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.key.Keyer
import coil.request.Options
import okio.Buffer

/** Coil model: load the embedded album art from the audio file at [uri]. */
data class EmbeddedArt(val uri: String)

/** Extracts embedded cover art from an audio file via [MediaMetadataRetriever]. */
class EmbeddedArtFetcher(
    private val data: EmbeddedArt,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val retriever = MediaMetadataRetriever()
        return try {
            // MediaMetadataRetriever throws RuntimeException for bad/unreadable
            // sources; a missing picture is a normal, expected null result.
            val bytes = runCatching {
                retriever.setDataSource(options.context, Uri.parse(data.uri))
                retriever.embeddedPicture
            }.getOrNull() ?: return null

            SourceResult(
                source = ImageSource(Buffer().apply { write(bytes) }, options.context),
                mimeType = null,
                dataSource = DataSource.DISK,
            )
        } finally {
            retriever.release()
        }
    }

    class Factory : Fetcher.Factory<EmbeddedArt> {
        override fun create(data: EmbeddedArt, options: Options, imageLoader: ImageLoader): Fetcher =
            EmbeddedArtFetcher(data, options)
    }
}

/** Stable Coil cache key per source file. */
class EmbeddedArtKeyer : Keyer<EmbeddedArt> {
    override fun key(data: EmbeddedArt, options: Options): String = "embedded-art:${data.uri}"
}
