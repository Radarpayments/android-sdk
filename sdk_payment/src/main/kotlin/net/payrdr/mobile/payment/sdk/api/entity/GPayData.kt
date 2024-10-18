package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * DTO for GPay response.
 *
 * @param orderId order identifier.
 * @param is3DSVer2 allow 3ds payment version 2.
 * @param acsUrl automatic configuration server url.
 * @param paReq params request.
 * @param termUrl terminal url.
 * @param errorTypeName error type.
 * @param threeDSAcsTransactionId transaction identifier into ACS.
 * @param threeDSAcsRefNumber identifier ACS.
 * @param threeDSAcsSignedContent sign content for ACS.
 * @param threeDSServerTransId server transaction id for ACS.
 */
data class GPayData(
    val orderId: String,
    val is3DSVer2: Boolean,
    val acsUrl: String?,
    val paReq: String?,
    val termUrl: String?,
    val errorTypeName: String?,
    val threeDSAcsTransactionId: String?,
    val threeDSAcsRefNumber: String?,
    val threeDSAcsSignedContent: String?,
    val threeDSServerTransId: String?,
) {
    companion object {
        /**
         * Converting data from JSON to object [GPayData].
         * @param jsonObject json-object, with fields of [GPayData] class.
         */
        fun fromJson(jsonObject: JSONObject): GPayData = with(jsonObject) {
            GPayData(
                orderId = getString("orderId"),
                is3DSVer2 = optBoolean("is3DSVer2"),
                acsUrl = optValue("acsUrl"),
                paReq = optValue("paReq"),
                termUrl = optValue("termUrl"),
                errorTypeName = optValue("errorTypeName"),
                threeDSAcsTransactionId = optValue("threeDSAcsTransactionId"),
                threeDSAcsRefNumber = optValue("threeDSAcsRefNumber"),
                threeDSAcsSignedContent = optValue("threeDSAcsSignedContent"),
                threeDSServerTransId = optValue("threeDSServerTransId") ?: optValue("threeDSDsTransID")
            )
        }
    }
}
