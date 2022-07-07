package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * Error when Merchant is not configured to be used without 3DS2SDK.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class SDKNotConfigureException(override val message: String = "", override val cause: Throwable?) :
    SDKException(message, cause)
