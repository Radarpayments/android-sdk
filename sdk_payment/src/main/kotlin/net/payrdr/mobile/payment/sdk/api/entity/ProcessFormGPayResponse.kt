package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * DTO for GPay payment response.
 *
 * @param success status of payment.
 * @param data GPay information [GPayData].
 */
data class ProcessFormGPayResponse(
    val success: String,
    val data: GPayData?
) {
    companion object {
        /**
         * Converting data from JSON to object [ProcessFormGPayResponse].
         * @param jsonObject json-object, with fields of [ProcessFormGPayResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): ProcessFormGPayResponse = with(jsonObject) {
            ProcessFormGPayResponse(
                success = getString("success"),
                data = if (jsonObject.has("data")) GPayData.fromJson(jsonObject.getJSONObject("data")) else null
            )
        }
    }
}
