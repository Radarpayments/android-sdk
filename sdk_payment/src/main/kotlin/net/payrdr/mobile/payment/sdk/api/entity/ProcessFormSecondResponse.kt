package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * Response DTO for requesting payment by new card with 3ds.
 *
 * @param errorCode error code.
 * @param is3DSVer2 there is 3DS.
 * @param redirect redirect url.
 * @param errorTypeName error type.
 * @param threeDSAcsTransactionId transaction identifier into ACS.
 * @param threeDSAcsRefNumber identifier ACS.
 * @param threeDSAcsSignedContent sign content for ACS.
 * @param threeDSServerTransId server transaction id for ACS.
 */
data class ProcessFormSecondResponse(
    val errorCode: Int,
    val is3DSVer2: Boolean,
    val redirect: String?,
    val errorTypeName: String?,
    val threeDSAcsTransactionId: String?,
    val threeDSAcsRefNumber: String?,
    val threeDSAcsSignedContent: String?,
    val threeDSServerTransId: String?,
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
                redirect = optValue("redirect"),
                errorTypeName = optValue("errorTypeName"),
                threeDSAcsTransactionId = optValue("threeDSAcsTransactionId"),
                threeDSAcsRefNumber = optValue("threeDSAcsRefNumber"),
                threeDSAcsSignedContent = optValue("threeDSAcsSignedContent"),
                threeDSServerTransId = optValue("threeDSServerTransId") ?: optValue("threeDSDsTransID")
            )
        }
    }
}
