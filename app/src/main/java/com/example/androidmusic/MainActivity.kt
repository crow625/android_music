package com.example.androidmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.androidmusic.ui.navigation.AppNavHost
import com.example.androidmusic.ui.theme.AndroidMusicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidMusicTheme {
                AppNavHost()
            }
        }
    }
}
