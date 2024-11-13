package net.payrdr.mobile.payment.sdk.logs.sentry.dto

import com.google.gson.annotations.SerializedName

data class SentryUploadUser(
    @SerializedName("id") val id: String
)
