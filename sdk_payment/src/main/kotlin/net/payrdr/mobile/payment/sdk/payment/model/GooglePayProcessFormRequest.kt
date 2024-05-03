package net.payrdr.mobile.payment.sdk.payment.model

/**
 *  An object containing data for API requests GPay.
 *
 *  @param paymentToken cryptogram created by GPay lib.
 *  @param mdOrder order number.
 */
data class GooglePayProcessFormRequest(
    val paymentToken: String,
    val mdOrder: String
)
