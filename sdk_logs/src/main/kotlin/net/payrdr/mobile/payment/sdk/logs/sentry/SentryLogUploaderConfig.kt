package net.payrdr.mobile.payment.sdk.logs.sentry

class SentryLogUploaderConfig(
    val url: String,
    val key: String,
    val appId: String,
) {
    init {
        check(url.isNotBlank()) { "Please define url value" }
        check(key.isNotBlank()) { "Please define key value" }
        check(appId.isNotBlank()) { "Please define appId value" }
    }
}
