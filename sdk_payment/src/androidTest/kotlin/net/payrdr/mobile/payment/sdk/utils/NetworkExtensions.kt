package net.payrdr.mobile.payment.sdk.utils

import java.io.BufferedOutputStream
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection

fun URL.executePostJsonForSessionId(
    jsonParamBody: String,
    apiKey: String,
    version: String
): HttpsURLConnection =
    (openConnection() as HttpsURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("X-Api-Key", apiKey)
        setRequestProperty("X-Version", version)
        doOutput = true
        setChunkedStreamingMode(0)
        OutputStreamWriter(BufferedOutputStream(outputStream)).use {
            it.write(jsonParamBody)
            it.flush()
            it.close()
        }
    }
