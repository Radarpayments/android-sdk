package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * Response DTO for google payment settings.
 *
 * @param environment the type of work environment.
 * @param merchantId the identifier of merchant.
 * @param gateway name of merchant.
 */
class GPaySettings(
    val environment: String,
    val merchantId: String,
    val gateway: String
) {
    companion object {
        /**
         * Converting data from JSON to object [GPayData].
         * @param jsonObject json-object, with fields of [GPayData] class.
         */
        fun fromJson(jsonObject: JSONObject): GPaySettings =
            with(jsonObject.getJSONObject("settings")) {
                GPaySettings(
                    environment = getString("googlePay.environment"),
                    merchantId = getString("googlePay.merchantId"),
                    gateway = getString("googlePay.gateway")
                )
            }
    }
}
