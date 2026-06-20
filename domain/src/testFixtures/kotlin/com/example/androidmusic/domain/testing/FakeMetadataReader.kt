package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.metadata.MetadataReader
import com.example.androidmusic.domain.metadata.TrackMetadata
import com.example.androidmusic.domain.model.MediaUri

class FakeMetadataReader(
    var metadataByUri: Map<String, TrackMetadata> = emptyMap(),
) : MetadataReader {
    override suspend fun read(uri: MediaUri): TrackMetadata? = metadataByUri[uri.value]
}
