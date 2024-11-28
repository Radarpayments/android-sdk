package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * Response DTO for customer information.
 * @param email customer email.
 * @param phone customer phone.
 */
data class CustomerDetails(
    val email: String?,
    val phone: String?
) {

    companion object {
        /**
         * Function to create [CustomerDetails] from JSON.
         * @param jsonObject json-object, with fields of [CustomerDetails] class.
         */
        fun fromJson(jsonObject: JSONObject?) = CustomerDetails(
            email = jsonObject?.optValue("email"),
            phone = jsonObject?.optValue("phone")
        )
    }
}
