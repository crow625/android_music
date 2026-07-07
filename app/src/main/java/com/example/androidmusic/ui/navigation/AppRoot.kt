package com.example.androidmusic.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidmusic.ui.albums.AlbumDetailRoute
import com.example.androidmusic.ui.albums.AlbumsRoute
import com.example.androidmusic.ui.artists.ArtistDetailRoute
import com.example.androidmusic.ui.artists.ArtistsRoute
import com.example.androidmusic.ui.folders.FolderDetailRoute
import com.example.androidmusic.ui.folders.FoldersRoute
import com.example.androidmusic.ui.library.LibraryRoute
import com.example.androidmusic.ui.player.MiniPlayer
import com.example.androidmusic.ui.player.NowPlayingRoute
import com.example.androidmusic.ui.player.PlayerViewModel
import com.example.androidmusic.ui.playlists.PlaylistDetailRoute
import com.example.androidmusic.ui.playlists.PlaylistsRoute
import com.example.androidmusic.ui.sources.SourcesRoute

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private val topLevelDestinations = listOf(
    TopLevelDestination(Destinations.LIBRARY, "Library", Icons.Filled.LibraryMusic),
    TopLevelDestination(Destinations.ALBUMS, "Albums", Icons.Filled.Album),
    TopLevelDestination(Destinations.ARTISTS, "Artists", Icons.Filled.Person),
    TopLevelDestination(Destinations.FOLDERS, "Folders", Icons.Filled.FolderOpen),
    TopLevelDestination(Destinations.PLAYLISTS, "Playlists", Icons.AutoMirrored.Filled.PlaylistPlay),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(navController: NavHostController = rememberNavController()) {
    val currentRoute by navController.currentRouteAsState()
    val isTopLevel = currentRoute == null || topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleFor(currentRoute)) },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isTopLevel) {
                        IconButton(onClick = {
                            navController.navigate(Destinations.SOURCES) { launchSingleTop = true }
                        }) {
                            Icon(Icons.Filled.Folder, contentDescription = "Folder sources")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (currentRoute != Destinations.NOW_PLAYING) {
                Column {
                    val playerViewModel: PlayerViewModel = hiltViewModel()
                    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
                    MiniPlayer(
                        state = playerState,
                        onPlayPause = playerViewModel::onPlayPause,
                        onNext = playerViewModel::onNext,
                        onClick = {
                            navController.navigate(Destinations.NOW_PLAYING) { launchSingleTop = true }
                        },
                    )
                    NavigationBar {
                        topLevelDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = { navController.navigateTopLevel(destination.route) },
                                icon = { Icon(destination.icon, contentDescription = destination.label) },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.LIBRARY,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destinations.LIBRARY) {
                LibraryRoute(onOpenSources = { navController.navigate(Destinations.SOURCES) })
            }
            composable(Destinations.ALBUMS) {
                AlbumsRoute(onOpenAlbum = { navController.navigate(Destinations.albumDetail(it)) })
            }
            composable(Destinations.ARTISTS) {
                ArtistsRoute(onOpenArtist = { navController.navigate(Destinations.artistDetail(it)) })
            }
            composable(Destinations.FOLDERS) {
                FoldersRoute(onOpenFolder = { navController.navigate(Destinations.folderDetail(it)) })
            }
            composable(Destinations.PLAYLISTS) {
                PlaylistsRoute(onOpenPlaylist = { navController.navigate(Destinations.playlistDetail(it)) })
            }
            composable(Destinations.SOURCES) { SourcesRoute() }
            composable(Destinations.NOW_PLAYING) { NowPlayingRoute() }
            composable(
                route = Destinations.ALBUM_DETAIL,
                arguments = listOf(navArgument(Destinations.ALBUM_ID_ARG) { type = NavType.StringType }),
            ) {
                AlbumDetailRoute()
            }
            composable(
                route = Destinations.ARTIST_DETAIL,
                arguments = listOf(navArgument(Destinations.ARTIST_ID_ARG) { type = NavType.StringType }),
            ) {
                ArtistDetailRoute(onOpenAlbum = { navController.navigate(Destinations.albumDetail(it)) })
            }
            composable(
                route = Destinations.FOLDER_DETAIL,
                arguments = listOf(navArgument(Destinations.FOLDER_URI_ARG) { type = NavType.StringType }),
            ) {
                FolderDetailRoute()
            }
            composable(
                route = Destinations.PLAYLIST_DETAIL,
                arguments = listOf(navArgument(Destinations.PLAYLIST_ID_ARG) { type = NavType.StringType }),
            ) {
                PlaylistDetailRoute(onDeleted = { navController.navigateUp() })
            }
        }
    }
}

@Composable
private fun NavHostController.currentRouteAsState(): androidx.compose.runtime.State<String?> {
    val backStackEntry by currentBackStackEntryAsState()
    return androidx.compose.runtime.rememberUpdatedState(backStackEntry?.destination?.route)
}

private fun NavHostController.navigateTopLevel(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        // Pop back to the start destination (clearing any pushed screen like
        // Folder Sources) so tapping a tab always returns to that tab.
        popUpTo(graph.findStartDestination().id) { inclusive = false }
        launchSingleTop = true
    }
}

private fun titleFor(route: String?): String = when (route) {
    Destinations.ALBUMS -> "Albums"
    Destinations.ARTISTS -> "Artists"
    Destinations.PLAYLISTS -> "Playlists"
    Destinations.SOURCES -> "Folder sources"
    Destinations.NOW_PLAYING -> "Now Playing"
    Destinations.FOLDERS -> "Folders"
    Destinations.ALBUM_DETAIL -> "Album"
    Destinations.ARTIST_DETAIL -> "Artist"
    Destinations.FOLDER_DETAIL -> "Folder"
    Destinations.PLAYLIST_DETAIL -> "Playlist"
    else -> "Library"
}
