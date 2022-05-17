package net.payrdr.mobile.payment.sdk.form

/**
 * Basic error that can be returned in response when executing SDK methods.
 *
 * @param message error description.
 * @param cause the reason for the error.
 */
open class SDKException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)
