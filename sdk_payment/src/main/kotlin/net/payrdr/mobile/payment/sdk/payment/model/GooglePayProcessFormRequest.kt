package net.payrdr.mobile.payment.sdk.payment.model

import org.json.JSONObject

/**
 *  An object containing data for API requests GPay.
 *
 *  @param paymentToken cryptogram created by GPay lib.
 *  @param mdOrder order number.
 *  @param email payer email from Google Pay.
 *  @param phone payer phone number from Google Pay.
 */
data class GooglePayProcessFormRequest(
    val paymentToken: String,
    val mdOrder: String,
    val email: String? = null,
    val phone: String? = null
) {

    /**
     * Request body for google/paymentOrder.do.
     */
    fun toJson(): JSONObject = JSONObject().apply {
        put("paymentToken", paymentToken)
        put("mdOrder", mdOrder)
        val additionalParameters = JSONObject().apply {
            email?.let { put("email", it) }
            phone?.let { put("phone", it) }
        }
        if (additionalParameters.length() > 0) {
            put("additionalParameters", additionalParameters)
        }
    }
}
