package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * DTO about payer personal information.
 * @param mobilePhone payer mobile phone.
 */
data class OrderPayerData(val mobilePhone: String?) {

    companion object {
        /**
         * Function to create [OrderPayerData] from JSON.
         * @param jsonObject json-object, with fields of [OrderPayerData] class.
         */
        fun fromJson(jsonObject: JSONObject?): OrderPayerData = OrderPayerData(
            mobilePhone = jsonObject?.optString("mobilePhone")
        )
    }
}
