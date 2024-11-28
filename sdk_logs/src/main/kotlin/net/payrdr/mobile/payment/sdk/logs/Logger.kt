package net.payrdr.mobile.payment.sdk.logs

/**
 * Class for custom log.
 *
 * [logInterfaces] - list of loggers using for logging.
 *
 */

object Logger {

    private val logInterfaces: MutableList<LogInterface> = mutableListOf()

    /**
     *  Add log interface [logger] to list [logInterfaces].
     */
    fun addLogInterface(logger: LogInterface) = logInterfaces.add(logger)

    /**
     * Perform log message with debug prefix.
     * @param classMethod class where the log method was called.
     * @param tag module tag.
     * @param message log message.
     * @param exception caused exception.
     * @param source the source of log event: [Source.WEB_VIEW] or [Source.NATIVE].
     */
    @JvmOverloads
    fun debug(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source = Source.NATIVE
    ) {
        logInterfaces.logSafety { logger ->
            logger.debug(classMethod, tag, message, exception, source)
        }
    }

    /**
     * Perform log message with error prefix.
     * @param classMethod class where the log method was called.
     * @param tag module tag.
     * @param message log message.
     * @param exception caused exception.
     * @param source the source of log event: [Source.WEB_VIEW] or [Source.NATIVE].
     */
    @JvmOverloads
    fun error(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source = Source.NATIVE
    ) {
        logInterfaces.logSafety { logger ->
            logger.error(classMethod, tag, message, exception, source)
        }
    }

    /**
     * Perform log message with warning prefix.
     * @param classMethod class where the log method was called.
     * @param tag module tag.
     * @param message log message.
     * @param exception caused exception.
     * @param source the source of log event: [Source.WEB_VIEW] or [Source.NATIVE].
     */
    @JvmOverloads
    fun warning(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source = Source.NATIVE
    ) {
        logInterfaces.logSafety { logger ->
            logger.warning(classMethod, tag, message, exception, source)
        }
    }

    /**
     * Perform log message with info prefix.
     * @param classMethod class where the log method was called.
     * @param tag module tag.
     * @param message log message.
     * @param exception caused exception.
     * @param source the source of log event: [Source.WEB_VIEW] or [Source.NATIVE].
     */
    @JvmOverloads
    fun info(
        classMethod: Class<*>,
        tag: String,
        message: String,
        exception: Throwable?,
        source: Source = Source.NATIVE
    ) {
        logInterfaces.logSafety { logger ->
            logger.info(classMethod, tag, message, exception, source)
        }
    }

    /**
     * Upload all saved log messages.
     */
    fun uploadLogs() {
        logInterfaces.forEach { logger ->
            logger.uploadLogs()
        }
    }
}
