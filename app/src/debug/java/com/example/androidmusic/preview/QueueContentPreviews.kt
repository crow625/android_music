package com.example.androidmusic.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmusic.ui.player.QueueContent
import com.example.androidmusic.ui.player.QueueItemUi
import com.example.androidmusic.ui.theme.AndroidMusicTheme

@Preview(name = "Queue content", showBackground = true)
@Composable
fun QueueContentPreview() {
    AndroidMusicTheme(dynamicColor = false) {
        QueueContent(
            queue = listOf(
                QueueItemUi(0, "1", "Chop Suey!", "System of a Down", isCurrent = true),
                QueueItemUi(1, "2", "Toxicity", "System of a Down", isCurrent = false),
                QueueItemUi(2, "3", "Aerials", "System of a Down", isCurrent = false),
            ),
            onJumpTo = {},
            onRemove = {},
            onClear = {},
        )
    }
}
