package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * An error that occurs when trying to re-pay order.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class SDKAlreadyPaymentException(
    override val message: String = "",
    override val cause: Throwable?
) :
    SDKException(message, cause)
