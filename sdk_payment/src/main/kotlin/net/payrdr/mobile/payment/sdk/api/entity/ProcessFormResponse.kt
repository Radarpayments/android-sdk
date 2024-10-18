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
 * @param acsUrl automatic configuration server url.
 * @param paReq params request.
 * @param termUrl terminal url.
 * @param threeDSMethodURL if not null, then merchant doesn't have 3ds configured.
 */
data class ProcessFormResponse(
    val errorCode: Int,
    val is3DSVer2: Boolean,
    val threeDSServerTransId: String?,
    val threeDSSDKKey: String?,
    val acsUrl: String?,
    val paReq: String?,
    val termUrl: String?,
    val threeDSMethodURL: String?,
) {

    companion object {
        /**
         * Converting data from JSON to object [ProcessFormResponse].
         * @param jsonObject json-object, with fields of [ProcessFormResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): ProcessFormResponse = with(jsonObject) {
            ProcessFormResponse(
                errorCode = getInt("errorCode"),
                is3DSVer2 = optBoolean("is3DSVer2"),
                threeDSServerTransId = optValue("threeDSServerTransId"),
                threeDSSDKKey = optValue("threeDSSDKKey"),
                acsUrl = optValue("acsUrl"),
                paReq = optValue("paReq"),
                termUrl = optValue("termUrl"),
                threeDSMethodURL = optValue("threeDSMethodURL"),
            )
        }
    }
}
