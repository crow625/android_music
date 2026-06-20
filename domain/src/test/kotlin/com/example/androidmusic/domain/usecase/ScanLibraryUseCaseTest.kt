package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.model.SourceFolder
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakeSourceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ScanLibraryUseCaseTest {

    @Test
    fun `scans the current source uris and returns the report`() = runTest {
        val sources = FakeSourceRepository(
            listOf(
                SourceFolder(MediaUri("tree://a"), Instant.EPOCH),
                SourceFolder(MediaUri("tree://b"), Instant.EPOCH),
            ),
        )
        val report = ScanReport(
            indexed = 5,
            skippedUnsupported = 1,
            metadataFailed = 0,
            unreadable = 0,
            problems = emptyList(),
        )
        val files = FakeAudioFileRepository(scanReport = report)

        val result = ScanLibraryUseCase(sources, files).invoke()

        assertEquals(report, result)
        assertEquals(listOf(MediaUri("tree://a"), MediaUri("tree://b")), files.lastScannedSources)
    }
}
