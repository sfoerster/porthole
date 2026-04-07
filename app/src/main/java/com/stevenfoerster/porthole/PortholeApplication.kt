package com.stevenfoerster.porthole

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for Porthole.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt's code generation
 * and serve as the application-level dependency container.
 */
@HiltAndroidApp
class PortholeApplication : Application()
