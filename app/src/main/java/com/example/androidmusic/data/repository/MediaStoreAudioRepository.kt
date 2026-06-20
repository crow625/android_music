package com.example.androidmusic.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.androidmusic.data.db.TrackDao
import com.example.androidmusic.data.db.TrackEntity
import com.example.androidmusic.data.mapper.toAndroidUri
import com.example.androidmusic.data.mapper.toAudioFile
import com.example.androidmusic.data.mapper.toEntity
import com.example.androidmusic.data.mapper.toMediaUri
import com.example.androidmusic.domain.concurrency.DispatcherProvider
import com.example.androidmusic.domain.diagnostics.AppError
import com.example.androidmusic.domain.diagnostics.DiagnosticReporter
import com.example.androidmusic.domain.diagnostics.ErrorCategory
import com.example.androidmusic.domain.diagnostics.Severity
import com.example.androidmusic.domain.library.LibraryScanMerge
import com.example.androidmusic.domain.library.StableTrackId
import com.example.androidmusic.domain.metadata.MetadataReader
import com.example.androidmusic.domain.metadata.TrackMetadata
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanProblem
import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.time.Clock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [AudioFileRepository] backed by SAF (DocumentFile) enumeration + Room.
 * Named per the technical spec; uses the Storage Access Framework rather than
 * MediaStore so multiple arbitrary tree roots are supported.
 */
class MediaStoreAudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val metadataReader: MetadataReader,
    private val clock: Clock,
    private val dispatchers: DispatcherProvider,
    private val reporter: DiagnosticReporter,
) : AudioFileRepository {

    override fun observeLibrary(): Flow<Library> =
        trackDao.observeActive().map { rows -> Library(rows.map(TrackEntity::toAudioFile)) }

    override suspend fun listAudioFiles(folderUri: MediaUri): List<AudioFile> =
        withContext(dispatchers.io) {
            listDocuments(folderUri.toAndroidUri())
                .filter { isSupported(it.name) }
                .mapNotNull { doc -> readAudioFile(doc.uri.toMediaUri()) }
        }

    override suspend fun scanSources(sourceUris: List<MediaUri>): ScanReport =
        withContext(dispatchers.io) {
            val acc = ScanAccumulator()
            sourceUris.forEach { scanSource(it, acc) }
            val report = acc.toReport()
            if (acc.problems.isNotEmpty()) {
                reporter.report(
                    AppError(
                        category = ErrorCategory.Scan,
                        severity = Severity.Warn,
                        message = "Scan finished with ${acc.problems.size} problem file(s)",
                        stackTrace = null,
                        context = mapOf("indexed" to acc.indexed.toString()),
                        occurredAt = clock.now(),
                    ),
                )
            }
            report
        }

    private suspend fun scanSource(source: MediaUri, acc: ScanAccumulator) {
        val found = mutableListOf<String>()
        val entities = mutableListOf<TrackEntity>()
        listDocuments(source.toAndroidUri()).forEach { doc ->
            when (val result = classifyDocument(doc.name, doc.uri.toMediaUri(), source)) {
                DocResult.Skipped -> acc.skipped++
                is DocResult.Unreadable -> {
                    acc.unreadable++
                    acc.problems += ScanProblem(result.uri, "unreadable or unsupported codec")
                }
                is DocResult.Indexed -> {
                    found += result.entity.id
                    entities += result.entity
                    acc.indexed++
                }
            }
        }
        if (entities.isNotEmpty()) trackDao.upsert(entities)
        val stale = LibraryScanMerge.staleTrackIds(trackDao.idsForFolder(source.value), found)
        if (stale.isNotEmpty()) trackDao.markStale(stale)
    }

    private suspend fun classifyDocument(name: String?, fileUri: MediaUri, source: MediaUri): DocResult {
        if (!isSupported(name)) return DocResult.Skipped
        val metadata = metadataReader.read(fileUri) ?: return DocResult.Unreadable(fileUri)
        val id = metadata.stableId()
        val entity = metadata.toEntity(id, fileUri, fileUri.value, source, clock.now().toEpochMilli())
        return DocResult.Indexed(entity)
    }

    private sealed interface DocResult {
        data object Skipped : DocResult
        data class Unreadable(val uri: MediaUri) : DocResult
        data class Indexed(val entity: TrackEntity) : DocResult
    }

    private suspend fun readAudioFile(fileUri: MediaUri): AudioFile? {
        val metadata = metadataReader.read(fileUri) ?: return null
        return metadata
            .toEntity(metadata.stableId(), fileUri, fileUri.value, fileUri, clock.now().toEpochMilli())
            .toAudioFile()
    }

    private fun TrackMetadata.stableId(): String =
        StableTrackId.compute(title, artist, album, durationMs, musicBrainzRecordingId)

    private fun listDocuments(treeUri: Uri): List<DocumentFile> {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val files = mutableListOf<DocumentFile>()
        val stack = ArrayDeque<DocumentFile>()
        stack.addLast(root)
        while (stack.isNotEmpty()) {
            val dir = stack.removeLast()
            dir.listFiles().forEach { child ->
                if (child.isDirectory) stack.addLast(child) else files += child
            }
        }
        return files
    }

    private fun isSupported(name: String?): Boolean {
        val ext = name?.substringAfterLast('.', "")?.lowercase().orEmpty()
        return ext in SUPPORTED_EXTENSIONS
    }

    private class ScanAccumulator {
        var indexed = 0
        var skipped = 0
        var unreadable = 0
        val problems = mutableListOf<ScanProblem>()

        fun toReport() = ScanReport(
            indexed = indexed,
            skippedUnsupported = skipped,
            metadataFailed = 0,
            unreadable = unreadable,
            problems = problems,
        )
    }

    private companion object {
        val SUPPORTED_EXTENSIONS = setOf("mp3", "aac", "m4a", "flac", "ogg", "opus", "wav")
    }
}
