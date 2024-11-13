package net.payrdr.mobile.payment.sdk.logs

class SDKPaymentUncaughtExceptionHandler private constructor(
    private val previousHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {

    companion object {

        fun install() {
            Thread.setDefaultUncaughtExceptionHandler(
                SDKPaymentUncaughtExceptionHandler(
                    Thread.getDefaultUncaughtExceptionHandler()
                )
            )
        }

        private const val ACCEPTABLE_PACKAGE = "net.payrdr.mobile.payment.sdk"
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            if (isErrorAcceptable(e)) {
                Logger.error(
                    this.javaClass,
                    "UncaughtExceptionHandler",
                    "Global exception",
                    e,
                    Source.NATIVE
                )
                Logger.uploadLogs()
            }
        } catch (th: Throwable) {
            th.printStackTrace()
        }
        previousHandler?.uncaughtException(t, e)
    }

    private fun isErrorAcceptable(e: Throwable): Boolean {
        return e.stackTrace.any { it.className.contains(ACCEPTABLE_PACKAGE) }
    }

}