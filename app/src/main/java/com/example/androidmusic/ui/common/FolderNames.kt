package com.example.androidmusic.ui.common

import java.net.URLDecoder

/**
 * Best-effort human-readable name for a SAF folder URI: decode it and take the
 * last path segment (e.g. ".../primary%3AMusic%2FRock" -> "Rock",
 * "primary%3AMusic" -> "Music").
 */
fun folderDisplayName(uri: String): String {
    val decoded = runCatching { URLDecoder.decode(uri, "UTF-8") }.getOrDefault(uri)
    return decoded.substringAfterLast('/').substringAfterLast(':').ifBlank { decoded }
}
