package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.SourceFolder
import com.example.androidmusic.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

class FakeSourceRepository(initial: List<SourceFolder> = emptyList()) : SourceRepository {
    private val sources = MutableStateFlow(initial)

    override fun observeSources(): Flow<List<SourceFolder>> = sources

    override suspend fun addSource(uri: MediaUri) {
        if (sources.value.none { it.uri == uri }) {
            sources.value = sources.value + SourceFolder(uri, Instant.EPOCH)
        }
    }

    override suspend fun removeSource(uri: MediaUri) {
        sources.value = sources.value.filterNot { it.uri == uri }
    }
}
