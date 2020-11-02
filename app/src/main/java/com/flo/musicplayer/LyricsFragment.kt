package com.flo.musicplayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.flo.musicplayer.databinding.FragmentLyricsBinding
import org.koin.android.ext.android.getKoin
import org.koin.android.viewmodel.ViewModelParameter
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier



class LyricsFragment: BaseFragment() {

    private lateinit var binding: FragmentLyricsBinding

//    private val viewModel: MusicViewModel by viewModels({requireParentFragment()})
    private val viewModel: MusicViewModel by sharedViewModel()

    private val args: LyricsFragmentArgs by navArgs()

//    private val vm: MusicViewModel by navGraphViewModels(R.id.nav_graph)

    lateinit var mAdapter: LyricsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLyricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
//        mSeekBar = binding.seekBar
        mPlayBtn = binding.btnPlay
        binding.seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener)

        val data = args.lyrics.toList()

        binding.rvDetail.setHasFixedSize(true)
        binding.rvDetail.layoutManager = LinearLayoutManager(context)
        mAdapter = LyricsAdapter(viewModel.lyricsList) {

        }
        binding.rvDetail.adapter = mAdapter

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    override fun setMax(max: Int) {
        super.setMax(max)
        binding.seekBar.max = max
    }
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        Log.d("vv", "LyricsFragment setProgress = $progress")
        binding.seekBar.progress = progress
    }
}