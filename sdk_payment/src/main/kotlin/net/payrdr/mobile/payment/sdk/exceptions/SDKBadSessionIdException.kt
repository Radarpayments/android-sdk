package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.SDKException

/**
 * Basic error that can be returned when converting session id to order number.
 *
 * @param message error description.
 * @param cause the reason for the error.
 */

class SDKBadSessionIdException(override val message: String = "", override val cause: Throwable?) :
    SDKException(message, cause)
