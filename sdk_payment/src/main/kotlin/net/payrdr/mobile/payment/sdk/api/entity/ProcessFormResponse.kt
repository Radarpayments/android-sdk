package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * Response DTO for requesting payment by new card.
 *
 * @param errorCode error code.
 * @param is3DSVer2 there is 3DS.
 * @param threeDSServerTransId transaction identifier for 3DS Server.
 * @param threeDSSDKKey key to encrypt device data.
 *
 */
data class ProcessFormResponse(
    val errorCode: Int,
    val is3DSVer2: Boolean,
    val threeDSServerTransId: String?,
    val threeDSSDKKey: String?
) {

    companion object {
        /**
         * Converting data from JSON to object [ProcessFormResponse].
         * @param jsonObject json-object, with fields of [ProcessFormResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): ProcessFormResponse = with(jsonObject) {
            ProcessFormResponse(
                errorCode = getInt("errorCode"),
                is3DSVer2 = getBoolean("is3DSVer2"),
                threeDSServerTransId = optValue("threeDSServerTransId"),
                threeDSSDKKey = optValue("threeDSSDKKey")
            )
        }
    }
}
