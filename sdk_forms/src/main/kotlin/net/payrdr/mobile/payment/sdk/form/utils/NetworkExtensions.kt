package net.payrdr.mobile.payment.sdk.form.utils

import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

/**
 * Server successful response code.
 */
const val CODE_SUCCESS = 200

/**
 * Casting the response to the [JSONObject] object.
 *
 * @return a [JSONObject] instance of a successful response body or an error body.
 */
fun HttpURLConnection.responseBodyToJsonObject(): JSONObject {
    val br = BufferedReader(
        InputStreamReader(
            if (responseCode == CODE_SUCCESS) inputStream else errorStream
        )
    )
    val responseBody = br.use(BufferedReader::readText)
    return JSONObject(responseBody)
}

/**
 * Executing a GET request on a URL instance.
 *
 * @return returns [HttpURLConnection].
 */
fun URL.executeGet(sslContext: SSLContext? = null): HttpsURLConnection =
    (openConnection() as HttpsURLConnection).apply {
        sslContext?.let {
            sslSocketFactory = it.socketFactory
        }
        requestMethod = "GET"
        setChunkedStreamingMode(0)
    }

/**
 * Extension for making a POST request with passing parameters as a Json object.
 *
 * @param jsonBody json to send in the request body.
 * @return returns [HttpURLConnection].
 */
fun URL.executePostJson(jsonBody: String, sslContext: SSLContext? = null): HttpsURLConnection =
    (openConnection() as HttpsURLConnection).apply {
        sslContext?.let {
            sslSocketFactory = it.socketFactory
        }
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json")
        doOutput = true
        setChunkedStreamingMode(0)
        OutputStreamWriter(BufferedOutputStream(outputStream)).use {
            it.write(jsonBody)
            it.flush()
            it.close()
        }
    }

/**
 * Extension for performing a POST request with passing parameters in the form of a key-value.
 *
 * @param paramBody collection of parameters to send in the request body.
 * @return returns [HttpURLConnection].
 */
fun URL.executePostParams(paramBody: Map<String, String>, sslContext: SSLContext? = null): HttpsURLConnection =
    (openConnection() as HttpsURLConnection).apply {
        sslContext?.let {
            sslSocketFactory = it.socketFactory
        }
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        doOutput = true
        setChunkedStreamingMode(0)
        OutputStreamWriter(BufferedOutputStream(outputStream)).use {
            val params = paramBody.map { elem ->
                "${encode(elem.key, UTF_8.name())}=${encode(elem.value, UTF_8.name())}"
            }.joinToString(separator = "&")
            it.write(params)
            it.flush()
            it.close()
        }
    }
