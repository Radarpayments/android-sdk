package net.payrdr.mobile.payment.sdk.form.component

import net.payrdr.mobile.payment.sdk.form.utils.asStringList
import org.json.JSONObject

/**
 * Card style and type information.
 *
 * @param backgroundColor background color.
 * @param backgroundGradient gradient colors background.
 * @param backgroundLightness true if the background is light colors, otherwise false.
 * @param textColor the color of the text on the card, in the format #ffffff or #fff.
 * @param logo link to the card bank logo file.
 * @param logoInvert card bank logo file link for light background.
 * @param paymentSystem payment system name.
 * @param status response answer.
 */
data class CardInfo(
    val backgroundColor: String,
    val backgroundGradient: List<String>,
    val backgroundLightness: Boolean,
    val textColor: String,
    val logo: String,
    val logoInvert: String,
    val paymentSystem: String,
    val status: String
) {

    companion object {

        /**
         * Deserializing [CardInfo] from JSONObject.
         *
         * @param jsonObject json object with data to creating [CardInfo].
         * @return card information object.
         */
        fun fromJson(jsonObject: JSONObject): CardInfo = CardInfo(
            backgroundColor = jsonObject.getString("backgroundColor"),
            backgroundGradient = jsonObject.getJSONArray("backgroundGradient").asStringList(),
            backgroundLightness = jsonObject.getBoolean("backgroundLightness"),
            textColor = jsonObject.getString("textColor"),
            logo = jsonObject.getString("logo"),
            logoInvert = jsonObject.getString("logoInvert"),
            paymentSystem = jsonObject.getString("paymentSystem"),
            status = jsonObject.getString("status")
        )
    }
}
