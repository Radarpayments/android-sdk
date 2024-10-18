package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * DTO for binding card.
 *
 * @param id identifier for binding card.
 * @param label description of card number and expiry date.
 * @param paymentSystem payment system for client card.
 * @param cardHolder first and last name of cardholder.
 * @param createdDate date when card was save at server.
 * @param payerEmail cardholder email.
 * @param payerPhone cardholder phone.
 * @param isMaestro is the card maestro payment system.
 */
data class BindingItem(
    val id: String,
    val label: String,
    val paymentSystem: String,
    val cardHolder: String,
    val createdDate: Long,
    val payerEmail: String,
    val payerPhone: String,
    val isMaestro: Boolean
) {
    companion object {
        /**
         * Converting data from JSON to object [BindingItem].
         * @param jsonObject json-object, with fields of [BindingItem] class.
         */
        fun fromJson(jsonObject: JSONObject): BindingItem = with(jsonObject) {
            BindingItem(
                id = getString("id"),
                label = getString("label"),
                paymentSystem = getString("paymentSystem"),
                cardHolder = getString("cardHolder"),
                createdDate = getLong("createdDate"),
                payerEmail = getString("payerEmail"),
                payerPhone = getString("payerPhone"),
                isMaestro = optBoolean("isMaestro"),
            )
        }
    }
}
