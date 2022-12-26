package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * Response DTO for merchant information.
 *
 * @param merchantUrl the URL of merchant storage.
 * @param merchantFullName the name of merchant.
 * @param mcc mcc value.
 * @param merchantLogin the login of merchant in system.
 * @param captchaMode is available captcha.
 * @param custom if merchant has a custom rules.
 */
data class MerchantInfo(
    val merchantUrl: String,
    val merchantFullName: String,
    val mcc: String,
    val merchantLogin: String,
    val captchaMode: String,
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
                mcc = getString("mcc"),
                merchantLogin = getString("merchantLogin"),
                captchaMode = getString("captchaMode"),
                custom = getBoolean("custom")
            )
        }
    }
}
