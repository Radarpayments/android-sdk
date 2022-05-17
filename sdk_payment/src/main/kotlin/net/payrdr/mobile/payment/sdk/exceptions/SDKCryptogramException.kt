package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * An error that occurs during the creating a cryptogram.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class SDKCryptogramException(override val message: String = "", override val cause: Throwable?) :
    SDKException(message, cause)
