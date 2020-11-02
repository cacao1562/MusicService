package com.flo.musicplayer.di.module

import com.flo.musicplayer.MusicRepository
import com.flo.musicplayer.MusicViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { MusicRepository(get()) }
}

val viewModelModule = module {
    viewModel { MusicViewModel(get()) }
}
