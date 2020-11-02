package com.flo.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import com.flo.musicplayer.databinding.FragmentMusicPlayBinding
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import org.koin.android.viewmodel.ext.android.sharedViewModel


class MusicPlayFragment : Fragment(), MusicService.ICallback {

    /**
     * koin 라이브러리에서 지원하는 sharedViewModel 를 사용하면 fragment 에서 viewmodel 공유가능
     * activity에서는 viewModel()
     */
    private val viewModel: MusicViewModel by sharedViewModel()
    private var mMusicService: MusicService? = null
    private lateinit var binding: FragmentMusicPlayBinding

    private var isBinding = false
    private lateinit var adapter: LyricsAdapter

    lateinit var mData: Music

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
            mMusicService?.registerCallback(this@MusicPlayFragment)
            isBinding = true
//            if (mMusicService?.isPlaying()!!) {
//                mMusicService?.initSeekBar()
//            }
        }
    }


    private val mSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMusicPlayBinding.inflate(inflater, container, false)
        return binding.root
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



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("bbb", "onActivityCreated  $isBinding")

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        if (mMusicService == null) {
//            val intent = Intent(requireActivity(), MusicService::class.java)
//            activity?.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }

        binding.seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener)

        binding.rvMain.setHasFixedSize(true)
        binding.rvMain.layoutManager = LinearLayoutManager(context)
        binding.viewEmpty.setOnClickListener {
            if (viewModel.lyricsList.size > 0) {
                Log.d("vvv", "MusicPlayFragment count = ${viewModel.count}")
                val action = MusicPlayFragmentDirections.actionMusicPlayFragmentToLyricsFragment(viewModel.lyricsList.toTypedArray())
                findNavController().navigate(action)
            }
        }

        binding.btnPlay.setOnClickListener {
            changeIcon()
            mMusicService?.let {
                if (isBinding) {
                    it.playNpause()
                }else {
                    startService(mData)
                    Intent(requireActivity(), MusicService::class.java).also { intent ->
                        activity?.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
                    }
                }
            }
        }


        viewModel.music.observe(viewLifecycleOwner, Observer {
            Log.d("aaa", it.toString())

            mData = it
            startService(it)

//            if (mMusicService?.isPlaying()!!) return@Observer


//            mMusicService?.initMediaPlay(it.file ?: "", mCallback)
            adapter = LyricsAdapter(viewModel.lyricsList) { pos -> }
            binding.rvMain.adapter = adapter

        })

    }

    fun startService(data: Music) {
        binding.tvTitle.text = data.title
        binding.tvSinger.text = data.singer
//            binding.ivAlbum.load(it.image)

        var bitmap: Bitmap? = null
        val loader = ImageLoader(requireContext())
        val req = ImageRequest.Builder(requireContext())
            .data(data.image) // demo link
            .target { result ->
                binding.ivAlbum.setImageDrawable(result)
                bitmap = (result as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                bitmap!!.compress(CompressFormat.JPEG, 100, stream)
                val byteArray: ByteArray = stream.toByteArray()

                val intent = Intent(requireActivity(), MusicService::class.java)
//            val b = Bundle()
//            b.putString(MusicService.Intent_URL, it.file)
//            b.putParcelable("data", bitmap)
                intent.putExtra(MusicService.Intent_URL, data.file)
                intent.putExtra(MusicService.Intent_Title, data.title)
                intent.putExtra(MusicService.Intent_Singer, data.singer)
                intent.putExtra(MusicService.Intent_Image, byteArray)
                intent.putExtra(MusicService.Intent_Progress, binding.seekBar.progress)

//            intent.putExtras(b)
                if (Build.VERSION.SDK_INT >= 26) {
                    context?.startForegroundService(intent)
                } else {
                    context?.startService(intent)
                }
            }
            .build()
        val result = loader.enqueue(req)
    }

    /**
     * 여러번 scroll 방지하기 위해 가사가 바뀔때만 scroll하기 위해
     */
    private var prevIdx = 0

    fun scrollLyrics(progress: Int) {

        var prev = 0

        for ((idx,value) in viewModel.lyricsList.withIndex()) {

            if (prev <= progress && progress <= value.mSecond) {

                if (prevIdx == idx) break
                Log.d("nnn", "prevIdx = $prevIdx  idx = $idx")
                val pos = idx
                adapter.setTextColor(pos - 1)
                binding.rvMain.smoothScrollToPosition(idx)
                prevIdx = pos

                break
            }

            prev = value.mSecond
        }
    }

    override fun changeIcon() {
        mMusicService?.let {
            if (it.isPlaying()) {
                binding.btnPlay.setImageResource(R.drawable.ic_pause)
            }else {
                binding.btnPlay.setImageResource(R.drawable.ic_play)
            }
        }
    }

    override fun setMax(max: Int) {
        Log.d("bbb", "setMax $max")
        binding.seekBar.max = max
        binding.tvCurrentTime.text = getTimeString(0)
        binding.tvTotalTime.text = getTimeString(max)
    }

    override fun setProgress(progress: Int) {
        Log.d("bbb", "setProgress $progress")
        binding.seekBar.progress = progress
        binding.tvCurrentTime.text = getTimeString(progress)
        scrollLyrics(progress)
    }

    override fun unBindService() {
        if (isBinding) {
            activity?.unbindService(mConnection)
            isBinding = false
        }
    }


}