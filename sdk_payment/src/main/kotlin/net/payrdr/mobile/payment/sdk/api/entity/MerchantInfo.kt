package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * Response DTO for merchant information.
 *
 * @param merchantUrl the URL of merchant storage.
 * @param merchantFullName the name of merchant.
 * @param merchantLogin the login of merchant in system.
 * @param custom if merchant has a custom rules.
 */
data class MerchantInfo(
    val merchantUrl: String,
    val merchantFullName: String,
    val merchantLogin: String,
    val custom: Boolean
) {

    companion object {
        /**
         * Converting data from JSON to object [MerchantInfo].
         * @param jsonObject json-object, with fields of [MerchantInfo] class.
         */
        fun fromJson(jsonObject: JSONObject): MerchantInfo = with(jsonObject) {
            MerchantInfo(
                merchantUrl = getString("merchantUrl"),
                merchantFullName = getString("merchantFullName"),
                merchantLogin = getString("merchantLogin"),
                custom = getBoolean("custom")
            )
        }
    }
}
