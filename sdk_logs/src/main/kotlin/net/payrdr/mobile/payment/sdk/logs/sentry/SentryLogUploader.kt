package net.payrdr.mobile.payment.sdk.logs.sentry

import android.util.Log
import com.google.gson.Gson
import net.payrdr.mobile.payment.sdk.logs.InstallationIdProvider
import net.payrdr.mobile.payment.sdk.logs.LogUploader
import net.payrdr.mobile.payment.sdk.logs.sentry.dto.SentryUploaderRequestContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.UUID

class SentryLogUploader(
    private val logUploaderConfig: SentryLogUploaderConfig,
    private val installationIdProvider: InstallationIdProvider
) : LogUploader, Callback {
    private val url: String = logUploaderConfig.url
    private val authHeader: String =
        String.format(Locale.ENGLISH, AUTH_HEADER_PATTERN, SENTRY_VERSION, logUploaderConfig.key, SENTRY_CLIENT)
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val gson: Gson = Gson()


    override fun uploadLogs(logs: List<String>) {
        try {
            val sessionId = UUID.randomUUID().toString()
            val logsToSend: MutableMap<String, List<String>> = mutableMapOf()
            val chunkedLogs = chunkLogs(logs.reversed())
            chunkedLogs.forEachIndexed { index, chunkLog ->
                logsToSend[String.format(Locale.ENGLISH, PART_DIVIDER_PATTERN, index + 1)] = chunkLog
            }
            sendLog(logUploaderConfig.appId, logsToSend, sessionId, installationIdProvider.getInstallationId())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendLog(
        appId: String,
        logs: Map<String, List<String>>,
        eventId: String,
        installationId: String
    ) {
        try {
            val json =
                gson.toJson(
                    SentryUploaderRequestContent.create(
                        appId,
                        logs,
                        eventId,
                        installationId
                    )
                )
                    ?: return
            Log.d("SentryLogUploader", "json: $json")
            val request = Request.Builder()
                .addHeader("X-Sentry-Auth", authHeader)
                .url(this.url)
                .post(RequestBody.create(JSON, json.toByteArray()))
                .build()
            client.newCall(request).enqueue(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun chunkLogs(logs: List<String>): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var currentBatch = mutableListOf<String>()
        var currentBatchSize = 0L
        var resultSize = 0L

        for (log in logs) {
            val logSize = log.length * 2L
            if (currentBatchSize + logSize < MAX_BATCH_SIZE) {
                currentBatch.add(log)
                currentBatchSize += logSize
            } else {
                if (resultSize + currentBatchSize < MAX_LOG_SIZE) {
                    result.add(currentBatch)
                    resultSize += currentBatchSize
                    currentBatch = mutableListOf(log)
                    currentBatchSize = logSize
                } else {
                    currentBatch = mutableListOf()
                    break
                }
            }
        }
        if (currentBatch.isNotEmpty()) {
            result.add(currentBatch)
        }
        return result
    }

    override fun onFailure(call: Call, e: IOException) {
        Log.d("SentryLogUploader", "Upload fail", e)
    }

    override fun onResponse(call: Call, response: Response) {
        Log.d("SentryLogUploader", "Upload completed code:" + response.code.toString())
    }

    companion object {
        private const val PART_DIVIDER_PATTERN = "Logs.%d"
        private const val SENTRY_VERSION = 7
        private const val SENTRY_CLIENT = "sdk-uploader/0.1"
        private const val AUTH_HEADER_PATTERN =
            "Sentry sentry_version=%d, sentry_key=%s, sentry_client=%s"
        private val JSON = "application/json;charset=UTF-8".toMediaTypeOrNull()
        private const val MAX_BATCH_SIZE = 1024 * 16
        private const val MAX_LOG_SIZE = 1024 * 256
    }
}
