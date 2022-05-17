package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * Error when paying for a canceled order.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class SDKDeclinedException(override val message: String = "", override val cause: Throwable?) :
    SDKException(message, cause)
