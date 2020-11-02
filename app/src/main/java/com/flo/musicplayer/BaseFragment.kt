package com.flo.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.fragment.app.Fragment

open class BaseFragment: Fragment(), MusicService.ICallback {

    private var mMusicService: MusicService? = null
    private var isBinding = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d("bbb", "onServiceDisconnected")
            mMusicService?.stopHandler()
            mMusicService?.isBind = false
            mMusicService = null
            isBinding = false
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d("bbb", "onServiceConnected")
            val binder = p1 as MusicService.MusicPlayerBinder
            mMusicService = binder.getService()
            mMusicService?.registerCallback(this@BaseFragment)
            isBinding = true
//            if (mMusicService?.isPlaying()!!) {
//                mMusicService?.initSeekBar()
//            }
        }
    }
    lateinit var mSeekBar: AppCompatSeekBar
    lateinit var mPlayBtn: AppCompatImageButton

    val mSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromeUser: Boolean) {
            /**
             * fraomUser는 사용자가 seekbar를 움직였다는 값
             */
            if (fromeUser) {
//                mMusicService?.seekToProgress(progress)
            }
        }

        override fun onStartTrackingTouch(seekbar: SeekBar?) {
            mMusicService?.stopHandler()
        }

        override fun onStopTrackingTouch(seekbar: SeekBar?) {
            seekbar?.progress?.let {
                mMusicService?.seekToProgress(it)
                mMusicService?.updateSeekBar()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        mSeekBar = activity?.findViewById(R.id.seekBar)!!
//        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener)
    }

    override fun onStart() {
        super.onStart()
        Log.d("bbb", "onStart  $isBinding")
        if (!isBinding) {
            Intent(requireActivity(), MusicService::class.java).also { intent ->
                activity?.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("bbb", "onStop  $isBinding")
        /**
         * binding이 안된상태에서 unbind하면 오류
         */
        if (isBinding) {
            activity?.unbindService(mConnection)
            isBinding = false
        }
    }

    /**
     * 여러번 scroll 방지하기 위해 가사가 바뀔때만 scroll하기 위해
     */
    private var prevIdx = 0

//    fun scrollLyrics(progress: Int) {
//
//        var prev = 0
//
//        for ((idx,value) in viewModel.lyricsList.withIndex()) {
//
//            if (prev <= progress && progress <= value.mSecond) {
//
//                if (prevIdx == idx) break
//                Log.d("nnn", "prevIdx = $prevIdx  idx = $idx")
//                val pos = idx
//                adapter.setTextColor(pos - 1)
//                binding.rvMain.smoothScrollToPosition(idx)
//                prevIdx = pos
//
//                break
//            }
//
//            prev = value.mSecond
//        }
//    }

    override fun changeIcon() {
        mMusicService?.let {
            if (it.isPlaying()) {
                mPlayBtn.setImageResource(R.drawable.ic_pause)
            }else {
                mPlayBtn.setImageResource(R.drawable.ic_play)
            }
        }
    }

    override fun setMax(max: Int) {
        Log.d("bbb", "setMax $max")
//        mSeekBar.max = max
//        binding.tvCurrentTime.text = getTimeString(0)
//        binding.tvTotalTime.text = getTimeString(max)
    }

    override fun setProgress(progress: Int) {
        Log.d("bbb", "setProgress $progress")
//        mSeekBar.progress = progress
//        binding.tvCurrentTime.text = getTimeString(progress)
//        scrollLyrics(progress)
    }

    override fun unBindService() {
        if (isBinding) {
            activity?.unbindService(mConnection)
            isBinding = false
        }
    }
}