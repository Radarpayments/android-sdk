package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * DTO for GPay response.
 *
 * @param orderId order identifier.
 * @param bindingId identifier of bind creation.
 * @param transactionId transaction identifier.
 */
data class GPayData(
    val orderId: String,
    val bindingId: String,
    val transactionId: String
) {
    companion object {
        /**
         * Converting data from JSON to object [GPayData].
         * @param jsonObject json-object, with fields of [GPayData] class.
         */
        fun fromJson(jsonObject: JSONObject): GPayData = with(jsonObject) {
            GPayData(
                orderId = getString("orderId"),
                bindingId = getString("bindingId"),
                transactionId = getString("transactionId")
            )
        }
    }
}
