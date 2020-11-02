package com.flo.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

}

fun getTimeString(millis: Int): String? {
    val buf = StringBuffer()
//    val hours = (millis / (1000 * 60 * 60)).toInt()
//    val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
    val minutes = (millis / 1000) / 60 % 60.toInt()
//    val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
    val seconds = (millis / 1000) % 60.toInt()
//    val mseconds = (millis % 1000).toInt()
    buf
//        .append(String.format("%02d", hours))
//        .append(":")
        .append(String.format("%02d", minutes))
        .append(":")
        .append(String.format("%02d", seconds))
//        .append(":")
//        .append(String.format("%03d", mseconds))
    return buf.toString()
}