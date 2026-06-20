package com.example.androidmusic.data.mapper

import com.example.androidmusic.data.db.TrackEntity
import com.example.androidmusic.domain.metadata.TrackMetadata
import com.example.androidmusic.domain.model.MediaUri
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackMappersTest {

    @Test
    fun `entity maps to domain AudioFile`() {
        val entity = TrackEntity(
            id = "id1",
            title = "Title",
            artist = "Artist",
            album = "Album",
            albumArtist = "AlbumArtist",
            trackNumber = 3,
            discNumber = 2,
            durationMs = 240_000,
            filePath = "/music/x.flac",
            uri = "file:///music/x.flac",
            folderUri = "tree://root",
            albumArtUri = "content://art/1",
            mbRecordingId = "mb1",
            isStale = false,
            dateIndexed = 100,
        )

        val file = entity.toAudioFile()

        assertEquals("id1", file.id)
        assertEquals(MediaUri("file:///music/x.flac"), file.uri)
        assertEquals("Title", file.title)
        assertEquals(3, file.trackNumber)
        assertEquals(MediaUri("content://art/1"), file.albumArtUri)
    }

    @Test
    fun `metadata maps to entity with supplied identity and source`() {
        val metadata = TrackMetadata(
            title = "Title",
            artist = "Artist",
            album = "Album",
            albumArtist = "AlbumArtist",
            trackNumber = 1,
            discNumber = 1,
            durationMs = 200_000,
            musicBrainzRecordingId = "mb-9",
            albumArtUri = MediaUri("content://art/9"),
        )

        val entity = metadata.toEntity(
            id = "stable-id",
            uri = MediaUri("file:///a.mp3"),
            filePath = "/a.mp3",
            folderUri = MediaUri("tree://root"),
            dateIndexed = 42,
        )

        assertEquals("stable-id", entity.id)
        assertEquals("file:///a.mp3", entity.uri)
        assertEquals("tree://root", entity.folderUri)
        assertEquals("mb-9", entity.mbRecordingId)
        assertEquals("content://art/9", entity.albumArtUri)
        assertEquals(false, entity.isStale)
        assertEquals(42, entity.dateIndexed)
    }
}
