package com.stevenfoerster.porthole.di

import android.content.Context
import android.net.wifi.WifiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/** Hilt module providing application-scoped dependencies. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides an application-scoped [CoroutineScope] backed by [Dispatchers.Main]
     * with a [SupervisorJob] so that child coroutine failures don't cancel siblings.
     */
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Provides the system [WifiManager] for gateway resolution. */
    @Provides
    @Singleton
    fun provideWifiManager(@ApplicationContext context: Context): WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
}
