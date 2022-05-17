package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * Response DTO for requesting payment by binding card.
 *
 * @param errorCode error code.
 */
data class ProcessBindingFormResponse(
    val errorCode: Int
) {

    companion object {
        /**
         * Converting data from JSON to object [ProcessBindingFormResponse].
         * @param jsonObject json-object, with fields of [ProcessBindingFormResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): ProcessBindingFormResponse = with(jsonObject) {
            ProcessBindingFormResponse(
                errorCode = getInt("errorCode")
            )
        }
    }
}
