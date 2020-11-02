package com.flo.musicplayer

import android.app.Application
import com.flo.musicplayer.di.module.networkModule
import com.flo.musicplayer.di.module.repositoryModule
import com.flo.musicplayer.di.module.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(networkModule, repositoryModule, viewModelModule)
        }
    }
}