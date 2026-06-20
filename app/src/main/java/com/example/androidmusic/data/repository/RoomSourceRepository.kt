package com.example.androidmusic.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.androidmusic.data.db.SourceFolderDao
import com.example.androidmusic.data.db.SourceFolderEntity
import com.example.androidmusic.data.db.TrackDao
import com.example.androidmusic.data.mapper.toAndroidUri
import com.example.androidmusic.domain.concurrency.DispatcherProvider
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.diagnostics.warn
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.SourceFolder
import com.example.androidmusic.domain.repository.SourceRepository
import com.example.androidmusic.domain.time.Clock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class RoomSourceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sourceFolderDao: SourceFolderDao,
    private val trackDao: TrackDao,
    private val clock: Clock,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
) : SourceRepository {

    override fun observeSources(): Flow<List<SourceFolder>> =
        sourceFolderDao.observeAll().map { rows ->
            rows.map { SourceFolder(MediaUri(it.uri), Instant.ofEpochMilli(it.addedAt)) }
        }

    override suspend fun addSource(uri: MediaUri) = withContext(dispatchers.io) {
        persistPermission(uri.toAndroidUri())
        sourceFolderDao.insert(SourceFolderEntity(uri.value, clock.now().toEpochMilli()))
    }

    override suspend fun removeSource(uri: MediaUri) = withContext(dispatchers.io) {
        sourceFolderDao.delete(uri.value)
        // Remove its tracks from the library (the files on disk are untouched).
        trackDao.deleteByFolder(uri.value)
        releasePermission(uri.toAndroidUri())
    }

    private fun persistPermission(uri: Uri) {
        try {
            context.contentResolver
                .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            logger.warn(TAG, "Could not persist read permission for $uri", e)
        }
    }

    private fun releasePermission(uri: Uri) {
        try {
            context.contentResolver
                .releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            logger.warn(TAG, "Could not release read permission for $uri", e)
        }
    }

    private companion object {
        const val TAG = "SourceRepository"
    }
}
