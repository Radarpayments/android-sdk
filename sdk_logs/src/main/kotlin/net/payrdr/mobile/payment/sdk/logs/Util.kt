package net.payrdr.mobile.payment.sdk.logs

fun List<LogInterface>.logSafety(logAction: (logger: LogInterface) -> Unit) {
    this.forEach { logger ->
        try {
            logAction(logger)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}