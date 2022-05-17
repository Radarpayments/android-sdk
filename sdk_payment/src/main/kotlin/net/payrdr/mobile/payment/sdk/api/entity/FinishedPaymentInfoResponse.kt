package net.payrdr.mobile.payment.sdk.api.entity

import net.payrdr.mobile.payment.sdk.utils.optValue
import org.json.JSONObject

/**
 * Response DTO for status information about result of finish payment process.
 *
 * @param status payment status.
 * @param paymentDate date of payment.
 * @param approvalCode transaction code.
 * @param terminalId terminal identifier.
 * @param refNum transaction rm.
 * @param panMasked masked card number.
 * @param formattedAmount formatted order amount in order currency .
 * @param actionCodeDescription order code description .
 */
data class FinishedPaymentInfoResponse(
    val status: String?,
    val paymentDate: String?,
    val approvalCode: String?,
    val terminalId: String?,
    val refNum: String?,
    val panMasked: String?,
    val formattedAmount: String?,
    val actionCodeDescription: String?
) {

    companion object {

        /**
         * Converting data from JSON to object [FinishedPaymentInfoResponse].
         * @param jsonObject json-object, with fields of [FinishedPaymentInfoResponse] class.
         */
        fun fromJson(jsonObject: JSONObject): FinishedPaymentInfoResponse = with(jsonObject) {
            FinishedPaymentInfoResponse(
                status = optValue("status"),
                paymentDate = optValue("paymentDate"),
                approvalCode = optValue("approvalCode"),
                terminalId = optValue("terminalId"),
                refNum = optValue("refNum"),
                panMasked = optValue("panMasked"),
                formattedAmount = optValue("formattedAmount"),
                actionCodeDescription = optValue("actionCodeDescription")
            )
        }
    }
}
