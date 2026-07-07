package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.library.LibraryGroupings
import com.example.androidmusic.domain.model.Album
import com.example.androidmusic.domain.model.Artist
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Folder
import com.example.androidmusic.domain.repository.AudioFileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAlbumsUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(): Flow<List<Album>> =
        audioFileRepository.observeLibrary().map { LibraryGroupings.albums(it.tracks) }
}

class GetArtistsUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(): Flow<List<Artist>> =
        audioFileRepository.observeLibrary().map { LibraryGroupings.artists(it.tracks) }
}

class GetAlbumTracksUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(albumId: String): Flow<List<AudioFile>> =
        audioFileRepository.observeLibrary().map { LibraryGroupings.albumTracks(it.tracks, albumId) }
}

class GetArtistAlbumsUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(artistId: String): Flow<List<Album>> =
        audioFileRepository.observeLibrary().map { LibraryGroupings.artistAlbums(it.tracks, artistId) }
}

class GetArtistTracksUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(artistId: String): Flow<List<AudioFile>> =
        audioFileRepository.observeLibrary().map { LibraryGroupings.artistTracks(it.tracks, artistId) }
}

class GetFoldersUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(): Flow<List<Folder>> =
        audioFileRepository.observeLibrary().map { LibraryGroupings.folders(it.tracks) }
}

class GetFolderTracksUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(folderUri: String): Flow<List<AudioFile>> =
        audioFileRepository.observeLibrary().map { LibraryGroupings.folderTracks(it.tracks, folderUri) }
}
