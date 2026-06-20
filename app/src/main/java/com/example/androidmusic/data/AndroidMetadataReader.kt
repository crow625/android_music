package com.example.androidmusic.data

import android.content.Context
import android.media.MediaMetadataRetriever
import com.example.androidmusic.data.mapper.toAndroidUri
import com.example.androidmusic.domain.concurrency.DispatcherProvider
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.warn
import com.example.androidmusic.domain.metadata.MetadataReader
import com.example.androidmusic.domain.metadata.TrackMetadata
import com.example.androidmusic.domain.model.MediaUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [MetadataReader] backed by [MediaMetadataRetriever].
 *
 * Note: MediaMetadataRetriever cannot read custom tags, so the MusicBrainz
 * Recording ID is not available here and stable IDs fall back to the metadata
 * hash. A richer tag reader can supply it later without changing this seam.
 */
class AndroidMetadataReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
) : MetadataReader {

    @Suppress("TooGenericExceptionCaught") // MediaMetadataRetriever throws RuntimeException for bad sources
    override suspend fun read(uri: MediaUri): TrackMetadata? = withContext(dispatchers.io) {
        val androidUri = uri.toAndroidUri()
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, androidUri)
            TrackMetadata(
                title = retriever.string(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: fileName(uri),
                artist = retriever.string(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: UNKNOWN,
                album = retriever.string(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: UNKNOWN,
                albumArtist = retriever.string(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                    ?: retriever.string(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: UNKNOWN,
                trackNumber = retriever.intPart(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
                discNumber = retriever.intPart(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER),
                durationMs = retriever.string(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L,
                musicBrainzRecordingId = null,
                albumArtUri = null,
            )
        } catch (e: RuntimeException) {
            logger.warn(TAG, "Could not read metadata: ${uri.value}", e)
            null
        } finally {
            retriever.release()
        }
    }

    private fun MediaMetadataRetriever.string(key: Int): String? =
        extractMetadata(key)?.takeIf { it.isNotBlank() }

    private fun MediaMetadataRetriever.intPart(key: Int): Int =
        extractMetadata(key)?.substringBefore('/')?.trim()?.toIntOrNull() ?: 0

    private fun fileName(uri: MediaUri): String =
        uri.value.substringAfterLast('/').substringAfterLast("%2F").ifBlank { UNKNOWN }

    private companion object {
        const val TAG = "MetadataReader"
        const val UNKNOWN = "Unknown"
    }
}
