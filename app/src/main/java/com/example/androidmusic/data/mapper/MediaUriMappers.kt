package com.example.androidmusic.data.mapper

import android.net.Uri
import com.example.androidmusic.domain.model.MediaUri

/** Conversions between the domain [MediaUri] and Android's [Uri], at the data boundary. */
fun MediaUri.toAndroidUri(): Uri = Uri.parse(value)

fun Uri.toMediaUri(): MediaUri = MediaUri(toString())
