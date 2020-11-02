package com.flo.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.IOException


class MusicService : Service() {

    private val mBinder = MusicPlayerBinder()
    private var mMediaPlayer: MediaPlayer? = null
    private var mCallback: ICallback? = null
    private var mRunnable: Runnable? = null
    private var mHandler = Handler(Looper.getMainLooper())

    companion object {
        const val Intent_URL = "Intent_URL"
        const val Intent_Title = "Intent_Title"
        const val Intent_Singer = "Intent_Singer"
        const val Intent_Image = "Intent_Image"
        const val Intent_Progress = "Intent_Progress"
    }

    interface ICallback {
        fun setMax(max: Int)
        fun setProgress(progress: Int)
        fun unBindService()
        fun changeIcon()
    }

    private var mImgUrl: String? = ""
    private var mTitle: String? = ""
    private var mSinger: String? = ""
    private var mBitmap: Bitmap? = null
    private var mProgress: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("bbb", "onStartCommand")
        intent?.let {
            when (it.action) {

                "PAUSE" -> {
                    playNpause()
                }

                "CLOSE" -> {
                    mCallback?.unBindService()
                    stopForeground(true)
                    stopSelf()
                }

                else -> {

                    mImgUrl = intent.getStringExtra(Intent_URL)
                    mTitle = intent.getStringExtra(Intent_Title)
                    mSinger = intent.getStringExtra(Intent_Singer)
                    mProgress = intent.getIntExtra(Intent_Progress, 0)
                    val byteArray = intent.getByteArrayExtra(Intent_Image)
                    byteArray?.let {
                        mBitmap = BitmapFactory.decodeByteArray(byteArray, 0, it.size)
                    }
                }
            }



        }
        if (mMediaPlayer != null) {
            return START_NOT_STICKY
        }
        try {
            mMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(intent?.getStringExtra(Intent_URL))
                setOnPreparedListener(mPreparedListener)
                setOnCompletionListener { mp ->
                    mp.release()
                    onUnbind(intent)
                }
                prepareAsync()
            }

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        deliverNotification(R.drawable.ic_pause, "PAUSE")

        return START_NOT_STICKY
    }

    private fun deliverNotification(playicon: Int, playtitle: String) {

        val channelId = "channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /**
             * IMPORTANCE_LOW 알림 소리 안나게함
             */
            val notificationChannel = NotificationChannel(
                channelId,
                "Music Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
//            notificationChannel.enableVibration(true)
            notificationChannel.description = "FLO Music Player"
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            baseContext, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseIntent = Intent(this, MusicService::class.java)
        pauseIntent.action = "PAUSE"
        val pausePendingIntent = PendingIntent.getService(
            this, 0,
            pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val closeIntent = Intent(this, MusicService::class.java)
        closeIntent.action = "CLOSE"
        val closePendingIntent = PendingIntent.getService(
            this, 0,
            closeIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )



        val notification = NotificationCompat.Builder(baseContext, channelId)
            // Show controls on lock screen even when user hides sensitive content.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_audiotrack)
            .setLargeIcon(mBitmap)
            // Add media control buttons that invoke intents in your media service
            .addAction(R.drawable.ic_prev, "Previous", null) // #0
            .addAction(playicon, playtitle, pausePendingIntent) // #1
            .addAction(R.drawable.ic_next, "Next", null) // #2
            .addAction(R.drawable.ic_close, "Close", closePendingIntent) // #2
            // Apply the media style template
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1 /* #1: pause button \*/)
//                .setMediaSession(mediaSession.getSessionToken())
            )
//            .setContentIntent(pendingIntent)
            .setContentTitle(mTitle)
            .setContentText(mSinger)
//            .build()


        // foreground 서비스로 실행한다.
        startForeground(1, notification.build());

    }

    private val mPreparedListener = object : MediaPlayer.OnPreparedListener {
        override fun onPrepared(mp: MediaPlayer?) {
            mp?.seekTo(mProgress)
            mp?.start()
            initSeekBar()
            mCallback?.changeIcon()
        }
    }

    fun seekToProgress(progress: Int) {
        mMediaPlayer?.let {
            it.seekTo(progress)
        }
    }

    fun initSeekBar() {
        Log.d("bbb", "initSeekBar")
        isBind = true
        mCallback?.setMax(mMediaPlayer!!.duration)
        Log.d("aaa", "initSeekBar duration = ${getTimeString(mMediaPlayer!!.duration)}")
        updateSeekBar()
    }

    fun stopHandler() {
//        isBind = false
        mRunnable?.let { mHandler.removeCallbacks(it) }
    }

    var isBind = false

    fun updateSeekBar() {

        Log.d("bbb", "updateSeekBar")
//        if (!isBind) return

        mMediaPlayer?.let {
            mCallback?.setProgress(it.currentPosition)
            Log.d("aaa", "updateSeekBar currentPosition = ${getTimeString(it.currentPosition)}")
            if (it.isPlaying) {
                mRunnable = Runnable {
                    updateSeekBar()
                }
                mHandler.postDelayed(mRunnable!!, 500)
            }
        }

    }

    fun isPlaying(): Boolean {
        return if (mMediaPlayer == null) {
            false
        } else {
            mMediaPlayer!!.isPlaying
        }

    }

    /**
     * ICallback interface 등록
     */
    fun registerCallback(callbak: ICallback) {
        Log.d("bbb", "registerCallback")
        mCallback = callbak
    }

    fun initMediaPlay(url: String, callbak: ICallback) {
        mCallback = callbak

        try {
            mMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener(mPreparedListener)
                prepareAsync()
            }

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun playNpause() {
        mMediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                deliverNotification(R.drawable.ic_play, "PLAY")
            }else {
                mp.start()
                if (isBind) {
                    updateSeekBar()
                }
                deliverNotification(R.drawable.ic_pause, "PAUSE")
            }
            mCallback?.changeIcon()
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        Log.d("bbb", "onBind")
        isBind = true
        return mBinder
    }

    /**
     * return true해야 onRebind 호출된다
     * 기본값은 false
     */
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("bbb", "onUnbind")
        isBind = false
        stopHandler()
        return true
    }

    override fun onRebind(intent: Intent?) {
        Log.d("bbb", "onRebind")
        isBind = true
        mMediaPlayer?.let {
            Log.d("bbb", "onRebind not null")
            mCallback?.setMax(it.duration)
            mCallback?.setProgress(it.currentPosition)
            if (it.isPlaying) {
                updateSeekBar()
            }
            mCallback?.changeIcon()
        }
        super.onRebind(intent)
    }

    /**
     * 모든 client가 unbind 되면 호출됨
     */
    override fun onDestroy() {
        Log.d("bbb", "Service onDestroy")
        super.onDestroy()
        stopHandler()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mCallback?.changeIcon()
    }

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicService? {
            return this@MusicService
        }
    }

}