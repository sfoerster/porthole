package com.stevenfoerster.porthole.network

import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/** Opens [HttpURLConnection] instances so connectivity checks can be tested without real network IO. */
@Singleton
class HttpUrlConnectionFactory
    @Inject
    constructor() {
        fun open(url: String): HttpURLConnection = URL(url).openConnection() as HttpURLConnection
    }
