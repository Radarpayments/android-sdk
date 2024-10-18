package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.form.utils.asList
import net.payrdr.mobile.payment.sdk.form.utils.asStringList
import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * Response DTO for payment status.
 *
 * @param redirect url for redirect.
 * @param remainingSecs the number of seconds before the order expires.
 * @param orderNumber order number.
 * @param amount order amount.
 * @param bindingItems list of binding card from objects [bindingItems] class.
 * @param cvcNotRequired no input required for cvc.
 * @param otherWayEnabled other types of payment allowed.
 * @param bindingEnabled is it possible to draw a check mark for saving the card.
 * @param feeEnabled tax is included.
 * @param backUrl url for back redirect.
 * @param orderExpired order expired.
 * @param is3DSVer2 there is 3DS.
 * @param expirationDateCustomValidation is own validation enabled for card expiration.
 * @param currencyAlphaCode Country currency code.
 * @param merchantInfo information about merchant.
 * @param expirationDateCustomValidation is own validation enabled for card expiration.
 * @param bindingDeactivationEnabled allows unbinding the card.
 * @param merchantOptions list of available payment methods.
 */
data class SessionStatusResponse(
    val redirect: String? = null,
    val remainingSecs: Long? = null,
    val orderNumber: String? = null,
    val amount: String? = null,
    val bindingItems: List<BindingItem>? = null,
    val cvcNotRequired: Boolean,
    val otherWayEnabled: Boolean,
    val bindingEnabled: Boolean = false,
    val feeEnabled: Boolean,
    val backUrl: String? = null,
    val orderExpired: Boolean,
    val is3DSVer2: Boolean,
    val expirationDateCustomValidation: Boolean,
    val bindingDeactivationEnabled: Boolean = false,
    val currencyAlphaCode: String? = null,
    val merchantInfo: MerchantInfo,
    val merchantOptions: List<String>,
) {

    companion object {
        /**
         * Convert data from JSON to [SessionStatusResponse] object.
         * @param jsonObject json object containing the fields of the [SessionStatusResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): SessionStatusResponse = with(jsonObject) {
            SessionStatusResponse(
                redirect = optValue("redirect"),
                remainingSecs = optValue("remainingSecs"),
                orderNumber = optValue("orderNumber"),
                amount = optValue("amount"),
                bindingItems = jsonObject.getJSONArray("bindingItems")
                    .asList()
                    .map {
                        BindingItem.fromJson(it)
                    },
                cvcNotRequired = optBoolean("cvcNotRequired"),
                otherWayEnabled = optBoolean("otherWayEnabled"),
                bindingEnabled = optBoolean("bindingEnabled"),
                feeEnabled = optBoolean("feeEnabled"),
                backUrl = optValue("backUrl"),
                orderExpired = optBoolean("orderExpired"),
                is3DSVer2 = optBoolean("is3DSVer2"),
                expirationDateCustomValidation = optBoolean("expirationDateCustomValidation"),
                currencyAlphaCode = optValue("currencyAlphaCode"),
                merchantInfo = MerchantInfo.fromJson(getJSONObject("merchantInfo")),
                bindingDeactivationEnabled = optBoolean("bindingDeactivationEnabled"),
                merchantOptions = jsonObject.getJSONArray("merchantOptions").asStringList()
            )
        }
    }
}
