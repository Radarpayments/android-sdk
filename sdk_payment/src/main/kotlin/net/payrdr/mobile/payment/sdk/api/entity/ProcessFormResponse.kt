package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * Response DTO for requesting payment by new card.
 *
 * @param errorCode error code.
 * @param acsUrl automatic configuration server url.
 * @param paReq params request.
 * @param termUrl terminal url.
 * @param threeDSMethodURL if not null, then the merchant requires the native 3DS2 SDK challenge,
 *   which this SDK no longer supports — the caller throws [SDKNotConfigureException].
 */
data class ProcessFormResponse(
    val errorCode: Int,
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
                acsUrl = optValue("acsUrl"),
                paReq = optValue("paReq"),
                termUrl = optValue("termUrl"),
                threeDSMethodURL = optValue("threeDSMethodURL"),
            )
        }
    }
}
