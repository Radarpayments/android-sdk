package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

@SmallTest
class FinishedPaymentInfoResponseTest {

    @Test
    fun shouldParseApprovedStatus() {
        val json = JSONObject(
            """{"status":"APPROVED","paymentDate":"2024-01-15","panMasked":"654654***3843"}"""
        )

        val result = FinishedPaymentInfoResponse.fromJson(json)

        result.status shouldBe "APPROVED"
        result.paymentDate shouldBe "2024-01-15"
        result.panMasked shouldBe "654654***3843"
    }

    @Test
    fun shouldParseDeclinedStatus() {
        val json = JSONObject("""{"status":"DECLINED"}""")

        val result = FinishedPaymentInfoResponse.fromJson(json)

        result.status shouldBe "DECLINED"
        result.paymentDate shouldBe null
        result.panMasked shouldBe null
    }

    @Test
    fun shouldParseDepositedStatus() {
        val json = JSONObject("""{"status":"DEPOSITED"}""")

        val result = FinishedPaymentInfoResponse.fromJson(json)

        result.status shouldBe "DEPOSITED"
    }

    @Test
    fun shouldParseAllOptionalFields() {
        val json = JSONObject(
            """
            {
              "status": "APPROVED",
              "paymentDate": "2024-06-01 10:30:00",
              "approvalCode": "ABC123",
              "terminalId": "TERM001",
              "refNum": "REF0001",
              "panMasked": "411111***1111",
              "formattedAmount": "100.00 RUB",
              "actionCodeDescription": "Approved"
            }
            """.trimIndent()
        )

        val result = FinishedPaymentInfoResponse.fromJson(json)

        result.approvalCode shouldBe "ABC123"
        result.terminalId shouldBe "TERM001"
        result.refNum shouldBe "REF0001"
        result.formattedAmount shouldBe "100.00 RUB"
        result.actionCodeDescription shouldBe "Approved"
    }
}
