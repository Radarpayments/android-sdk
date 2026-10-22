package net.payrdr.mobile.payment.sdk

/**
 * An error that indicates that payment was canceled by user.
 *
 * @param message error description.
 * @param cause the reason for the error.
 */
class SDKCanceledByUserException(
    override val message: String = "Payment was canceled by user",
    override val cause: Throwable? = null
) : SDKException(message, cause)
