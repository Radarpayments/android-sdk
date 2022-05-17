package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * An error that occurs when calling API methods for making a payment.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class SDKPaymentApiException(override val message: String = "", override val cause: Throwable?) :
    SDKException(message, cause)
