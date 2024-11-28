package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONArray
import org.json.JSONObject

/**
 * Data about payer which can to be filled.
 * @param mastercard list of fields for mastercard payment system.
 * @param visa list of fields for visa payment system.
 */
data class PayerDataParamsNeedToBeFilled(
    val mastercard: List<FieldParams>?,
    val visa: List<FieldParams>?
) {

    companion object {
        /**
         * Function to create [PayerDataParamsNeedToBeFilled] from JSON.
         * @param jsonObject json-object, with fields of [PayerDataParamsNeedToBeFilled] class.
         */
        fun fromJson(jsonObject: JSONObject?): PayerDataParamsNeedToBeFilled {
            val mastercardFields = jsonObject?.optJSONArray("MASTERCARD")?.let {
                parseExtraCardFields(it)
            }
            val visaFields = jsonObject?.optJSONArray("VISA")?.let {
                parseExtraCardFields(it)
            }
            return PayerDataParamsNeedToBeFilled(
                mastercard = mastercardFields,
                visa = visaFields
            )
        }

        private fun parseExtraCardFields(jsonArray: JSONArray): List<FieldParams> {
            val fields = mutableListOf<FieldParams>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val mandatory = jsonObject.getBoolean("mandatory")
                val name = jsonObject.getString("name")
                fields.add(FieldParams(mandatory, name))
            }
            return fields
        }
    }
}
