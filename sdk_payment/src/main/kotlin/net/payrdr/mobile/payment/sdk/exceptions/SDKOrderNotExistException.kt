package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * An error that occurs with a non-existent order.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class SDKOrderNotExistException(override val message: String = "", override val cause: Throwable?) :
    SDKException(message, cause)
