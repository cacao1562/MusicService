package com.flo.musicplayer

class MusicRepository(private val musicApi: MusicApi) {
    suspend fun getMusic() = musicApi.getMusic()
}