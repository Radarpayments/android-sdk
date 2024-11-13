package net.payrdr.mobile.payment.sdk.logs.sentry

import net.payrdr.mobile.payment.sdk.logs.LogInterface
import net.payrdr.mobile.payment.sdk.logs.Source
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList


class SentryLogger(
    private val sentryLogUploader: SentryLogUploader,
    private val isWebViewLogsEnabled: Boolean,
) : LogInterface {

    private val logs: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)

    override fun debug(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    ) {
        log(classMethod, tag, "${LogInterface.DEBUG}: $message", exception, source)
    }

    override fun error(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    ) {
        log(classMethod, tag, "${LogInterface.ERROR}: $message", exception, source)
    }

    override fun warning(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    ) {
        log(classMethod, tag, "${LogInterface.WARNING}: $message", exception, source)
    }

    override fun info(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    ) {
        log(classMethod, tag, "${LogInterface.INFO}: $message", exception, source)
    }

    private fun log(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    ) {
        if (isWebViewLogsEnabled.not() && source == Source.WEB_VIEW) return
        logs.add(
            String.format(
                "%s %s %s %s %s %s",
                dateFormatter.format(Date().time),
                classMethod,
                tag,
                message,
                (exception?.toString() ?: ""),
                "Source: $source"
            ).trim()
        )
    }

    override fun uploadLogs() {
        sentryLogUploader.uploadLogs(logs)
        logs.clear()
    }
}
