package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * An error that occurs when initializing a transaction object with any key.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class SDKTransactionException(override val message: String = "", override val cause: Throwable?) :
    SDKException(message, cause)
