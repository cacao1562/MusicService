package com.flo.musicplayer

import retrofit2.http.GET

interface MusicApi {

    @GET("2020-flo/song.json")
    suspend fun getMusic(): Music
}