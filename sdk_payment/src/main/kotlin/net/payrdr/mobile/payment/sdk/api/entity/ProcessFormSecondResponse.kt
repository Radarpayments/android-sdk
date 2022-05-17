package net.payrdr.mobile.payment.sdk.api.entity

import org.json.JSONObject

/**
 * Response DTO for requesting payment by new card with 3ds.
 *
 * @param errorCode error code.
 * @param is3DSVer2 there is 3DS.
 * @param threeDSAcsTransactionId transaction identifier into ACS.
 * @param threeDSAcsRefNumber identifier ACS.
 * @param threeDSAcsSignedContent sign content for ACS.
 */
data class ProcessFormSecondResponse(
    val errorCode: Int,
    val is3DSVer2: Boolean,
    val threeDSAcsTransactionId: String,
    val threeDSAcsRefNumber: String,
    val threeDSAcsSignedContent: String
) {

    companion object {
        /**
         * Converting data from JSON to object [ProcessFormSecondResponse].
         * @param jsonObject json-object, with fields of [ProcessFormSecondResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): ProcessFormSecondResponse = with(jsonObject) {
            ProcessFormSecondResponse(
                errorCode = getInt("errorCode"),
                is3DSVer2 = getBoolean("is3DSVer2"),
                threeDSAcsTransactionId = getString("threeDSAcsTransactionId"),
                threeDSAcsRefNumber = getString("threeDSAcsRefNumber"),
                threeDSAcsSignedContent = getString("threeDSAcsSignedContent")
            )
        }
    }
}
