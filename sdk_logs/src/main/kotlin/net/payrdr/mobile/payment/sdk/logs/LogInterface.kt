package net.payrdr.mobile.payment.sdk.logs

interface LogInterface {

    fun debug(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    )

    fun error(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    )

    fun warning(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    )

    fun info(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source
    )

    fun uploadLogs()

    companion object {
        internal const val DEBUG = "DEBUG"
        internal const val ERROR = "ERROR"
        internal const val INFO = "INFO"
        internal const val WARNING = "WARNING"
    }
}
