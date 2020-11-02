package com.flo.musicplayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class MusicViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    var count = 0

    lateinit var musicData: Music

    val music: LiveData<Music> = liveData {
        musicData = musicRepository.getMusic()
        setLyrics(musicData)
        emit(musicData)
    }

    var lyricsList = mutableListOf<MusicLyrics>()

    fun setLyrics(data: Music) {

        val list = mutableListOf<MusicLyrics>()
        val sp = data.lyrics?.split("\n".toRegex())
        Log.d("bbb", sp.toString())
        sp?.let {

            for (s in it) {
                val time = s.substring(0, 10).replace("[", "").replace("]", "")
                val lyrics = s.substring(11)
                val timesp = time.split(":".toRegex())
                val msecond =
                    (timesp[0].toInt() * 60000) + (timesp[1].toInt() * 1000) + timesp[2].toInt()
                list.add(MusicLyrics(lyrics, msecond))
                Log.d(
                    "bbb",
                    "time = ${timesp[0].toInt()}, ${timesp[1].toInt()}, ${timesp[2].toInt()}  =  $msecond"
                )
            }

            lyricsList = list
        }
    }
}