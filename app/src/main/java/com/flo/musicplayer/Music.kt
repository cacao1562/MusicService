package com.flo.musicplayer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Music(
    val singer: String?,
    val album: String?,
    val title: String?,
    val duration: Int?,
    val image: String?,
    val file: String?,
    val lyrics: String?
)

@Parcelize
data class MusicLyrics(
    val lyrics: String,
    val mSecond: Int
): Parcelable
