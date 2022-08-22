package net.payrdr.mobile.payment.sdk.core

/**
 * Class for custom log.
 *
 * [tag] - module tag.
 * [Class] - class where the method was called.
 * [String] - log message.
 * [Exception] - caused exception.
 *
 */
object Logger {

    const val TAG = "SDK-Core"

    private val logInterfaces: MutableList<LogInterface> = mutableListOf()

    fun addLogInterface(logger: LogInterface) {
        logInterfaces.add(logger)
    }

    fun log(classMethod: Class<Any>, tag: String, message: String, exception: Exception?) {
        logInterfaces.forEach {
            it.log(classMethod, tag, message, exception)
        }
    }
}
