package net.payrdr.mobile.payment.sdk.core

/**
 * Interface for custom log.
 *
 * [tag] - module tag.
 * [Class] - class where the method was called.
 * [String] - log message.
 * [Exception] - caused exception.
 *
 */
interface LogInterface {

    /**
     *
     * Method signature for implementing logs.
     *
     */
    fun log(classMethod: Class<Any>, tag: String, message: String, exception: Exception?)
}
