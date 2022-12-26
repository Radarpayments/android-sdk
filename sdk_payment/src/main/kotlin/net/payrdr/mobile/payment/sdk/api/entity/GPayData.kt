package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * DTO for GPay response.
 *
 * @param orderId order identifier.
 * @param is3DSVer2 allow 3ds payment version 2.
 * @param acsUrl automatic configuration server url.
 * @param paReq params request.
 * @param termUrl terminal url.
 */
data class GPayData(
    val orderId: String,
    val is3DSVer2: Boolean,
    val acsUrl: String,
    val paReq: String,
    val termUrl: String,
) {
    companion object {
        /**
         * Converting data from JSON to object [GPayData].
         * @param jsonObject json-object, with fields of [GPayData] class.
         */
        fun fromJson(jsonObject: JSONObject): GPayData = with(jsonObject) {
            GPayData(
                orderId = getString("orderId"),
                is3DSVer2 = getBoolean("is3DSVer2"),
                acsUrl = getString("acsUrl"),
                paReq = getString("paReq"),
                termUrl = getString("termUrl"),
            )
        }
    }
}
