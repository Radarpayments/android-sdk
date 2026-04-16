package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

@SmallTest
class ProcessFormGPayResponseTest {

    @Test
    fun shouldParseSuccessResponseWithoutData() {
        val json = JSONObject("""{"success":"true"}""")

        val result = ProcessFormGPayResponse.fromJson(json)

        result.success shouldBe "true"
        result.data shouldBe null
    }

    @Test
    fun shouldParseResponseWithGPayData() {
        val json = JSONObject(
            """
            {
              "success": "true",
              "data": {
                "orderId": "order-xyz",
                "acsUrl": "https://acs.example.com",
                "paReq": "test-pareq",
                "termUrl": "https://term.example.com"
              }
            }
            """.trimIndent()
        )

        val result = ProcessFormGPayResponse.fromJson(json)

        result.success shouldBe "true"
        result.data!!.orderId shouldBe "order-xyz"
        result.data!!.acsUrl shouldBe "https://acs.example.com"
        result.data!!.paReq shouldBe "test-pareq"
        result.data!!.termUrl shouldBe "https://term.example.com"
    }

    @Test
    fun shouldParseResponseWithGPayError() {
        val json = JSONObject(
            """
            {
              "success": "false",
              "data": {
                "orderId": "order-xyz",
                "errorTypeName": "PAYMENT_ERROR"
              }
            }
            """.trimIndent()
        )

        val result = ProcessFormGPayResponse.fromJson(json)

        result.success shouldBe "false"
        result.data!!.orderId shouldBe "order-xyz"
        result.data!!.errorTypeName shouldBe "PAYMENT_ERROR"
        result.data!!.acsUrl shouldBe null
    }

    @Test
    fun shouldParse3DSFieldsInGPayData() {
        val json = JSONObject(
            """
            {
              "success": "true",
              "data": {
                "orderId": "order-3ds",
                "threeDSAcsTransactionId": "acs-tx-1",
                "threeDSAcsRefNumber": "ref-1",
                "threeDSAcsSignedContent": "signed-content",
                "threeDSServerTransId": "server-tx-1"
              }
            }
            """.trimIndent()
        )

        val result = ProcessFormGPayResponse.fromJson(json)

        result.data!!.threeDSAcsTransactionId shouldBe "acs-tx-1"
        result.data!!.threeDSAcsRefNumber shouldBe "ref-1"
        result.data!!.threeDSAcsSignedContent shouldBe "signed-content"
        result.data!!.threeDSServerTransId shouldBe "server-tx-1"
    }
}
