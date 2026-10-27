package net.payrdr.mobile.payment.sdk.exceptions

import net.payrdr.mobile.payment.sdk.SDKException

/**
 * The payment was declined: by the issuer, by 3-D Secure, by the gateway, or in a previous payment attempt.
 *
 * @param message error text for the payer, from the gateway.
 * @param cause error reason.
 * @param actionCode gateway result code of the payment attempt, e.g. "116".
 * @param errorType gateway error type of the payment attempt, e.g. "INSUFFICIENT_FUNDS".
 */
class SDKDeclinedException(
    override val message: String = "",
    override val cause: Throwable?,
    val actionCode: String? = null,
    val errorType: String? = null,
) : SDKException(message, cause)
