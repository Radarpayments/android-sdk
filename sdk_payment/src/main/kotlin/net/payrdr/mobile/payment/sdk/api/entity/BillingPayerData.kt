package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * Response DTO for pre-filled fields about payer.
 * @param billingAddressLine1 billing address details 1.
 * @param billingAddressLine2 billing address details 2.
 * @param billingAddressLine3 billing address details 3.
 * @param billingCity billing city.
 * @param billingCountry billing country.
 * @param billingPostalCode billing postal code.
 * @param billingState billing state.
 */
data class BillingPayerData(
    val billingAddressLine1: String?,
    val billingAddressLine2: String?,
    val billingAddressLine3: String?,
    val billingCity: String?,
    val billingCountry: String?,
    val billingPostalCode: String?,
    val billingState: String?
) {
    companion object {
        /**
         * Function to create [BillingPayerData] from JSON.
         * @param jsonObject json-object, with fields of [BillingPayerData] class.
         */
        fun fromJson(jsonObject: JSONObject?) = BillingPayerData(
            billingAddressLine3 = jsonObject?.optValue("billingAddressLine3"),
            billingAddressLine1 = jsonObject?.optValue("billingAddressLine1"),
            billingAddressLine2 = jsonObject?.optValue("billingAddressLine2"),
            billingCity = jsonObject?.optValue("billingCity"),
            billingCountry = jsonObject?.optValue("billingCountry"),
            billingState = jsonObject?.optValue("billingState"),
            billingPostalCode = jsonObject?.optValue("billingPostalCode")
        )
    }
}
