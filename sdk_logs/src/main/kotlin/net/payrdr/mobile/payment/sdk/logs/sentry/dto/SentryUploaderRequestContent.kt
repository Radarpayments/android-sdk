package net.payrdr.mobile.payment.sdk.logs.sentry.dto

import android.os.Build
import com.google.gson.annotations.SerializedName

class SentryUploaderRequestContent(
    @SerializedName("user") val user: SentryUploadUser,
    @SerializedName("message") val message: String,
    @SerializedName("extra") val extra: Map<String, List<String>>,
    @SerializedName("tags") val tags: Map<String, String>,
) {

    companion object {
        fun create(
            appId: String,
            partOfLogs: Map<String, List<String>>,
            sessionId: String,
            installationId: String
        ): SentryUploaderRequestContent {
            val device = Build.MANUFACTURER + " " + Build.MODEL
            return SentryUploaderRequestContent(
                user = SentryUploadUser(installationId),
                message = appId,
                extra = partOfLogs,
                tags = mapOf(
                    SESSION_ID to sessionId,
                    DEVICE to device,
                    OS to Build.VERSION.RELEASE,
                ),
            )
        }

        private const val SESSION_ID = "sessionId"
        private const val DEVICE = "device"
        private const val OS = "os"
    }
}
