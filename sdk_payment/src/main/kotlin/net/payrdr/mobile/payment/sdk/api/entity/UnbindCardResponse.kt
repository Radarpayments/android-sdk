package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.api.entity.UnbindCardResponse.Companion.SUCCESS_ERROR_CODE
import org.json.JSONObject

/**
 * Response DTO for unbind card method.
 * If errorCode equals [SUCCESS_ERROR_CODE] then the card was unbind.
 *
 * @param errorCode error code.
 */
class UnbindCardResponse(
    private val errorCode: Int
) {
    /**
     * Checking the result for success when sending an api request to unbinding a card.
     * @return [Boolean] value is result success.
     */
    fun isSuccess(): Boolean = errorCode == SUCCESS_ERROR_CODE

    companion object {
        /**
         *  The code at which the unbinding of the card was successful.
         */
        private const val SUCCESS_ERROR_CODE = 0

        /**
         * Converting data from JSON to object [UnbindCardResponse].
         * @param jsonObject json-object, with fields of [UnbindCardResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): UnbindCardResponse = with(jsonObject) {
            UnbindCardResponse(
                errorCode = getInt("errorCode")
            )
        }
    }
}
