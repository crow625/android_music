package com.example.androidmusic.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.androidmusic.image.EmbeddedArt

/**
 * Renders embedded album art for the track at [artworkUri], falling back to a
 * placeholder icon when there's no uri, no embedded art, or while loading.
 */
@Composable
fun AlbumArtwork(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier) {
        if (artworkUri == null) {
            ArtPlaceholder()
        } else {
            SubcomposeAsyncImage(
                model = EmbeddedArt(artworkUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { ArtPlaceholder() },
                error = { ArtPlaceholder() },
            )
        }
    }
}

@Composable
private fun ArtPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.Album,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
