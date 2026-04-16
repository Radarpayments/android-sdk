package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test

@SmallTest
class GPayDataTest {

    @Test
    @Description("parses all fields when present")
    fun `parses full response`() {
        val json = JSONObject(
            """
            {
              "orderId": "order-1",
              "acsUrl": "https://acs.example.com",
              "paReq": "pareq-value",
              "termUrl": "https://term.example.com",
              "errorTypeName": null,
              "threeDSAcsTransactionId": "acs-tx-1",
              "threeDSAcsRefNumber": "acs-ref-1",
              "threeDSAcsSignedContent": "signed-content",
              "threeDSServerTransId": "server-tx-1"
            }
            """.trimIndent()
        )

        val result = GPayData.fromJson(json)

        result.orderId shouldBe "order-1"
        result.acsUrl shouldBe "https://acs.example.com"
        result.paReq shouldBe "pareq-value"
        result.termUrl shouldBe "https://term.example.com"
        result.errorTypeName shouldBe null
        result.threeDSAcsTransactionId shouldBe "acs-tx-1"
        result.threeDSAcsRefNumber shouldBe "acs-ref-1"
        result.threeDSAcsSignedContent shouldBe "signed-content"
        result.threeDSServerTransId shouldBe "server-tx-1"
    }

    @Test
    @Description("threeDSServerTransId falls back to threeDSDsTransID when the primary field is absent")
    fun `falls back to threeDSDsTransID`() {
        val json = JSONObject(
            """
            {
              "orderId": "order-1",
              "threeDSDsTransID": "ds-trans-id-value"
            }
            """.trimIndent()
        )

        val result = GPayData.fromJson(json)

        result.orderId shouldBe "order-1"
        result.threeDSServerTransId shouldBe "ds-trans-id-value"
    }

    @Test
    @Description("primary threeDSServerTransId wins over threeDSDsTransID when both present")
    fun `primary wins over fallback`() {
        val json = JSONObject(
            """
            {
              "orderId": "order-1",
              "threeDSServerTransId": "primary",
              "threeDSDsTransID": "fallback"
            }
            """.trimIndent()
        )

        val result = GPayData.fromJson(json)

        result.threeDSServerTransId shouldBe "primary"
    }

    @Test(expected = JSONException::class)
    @Description("missing orderId throws JSONException")
    fun `missing orderId throws`() {
        val json = JSONObject("{}")
        GPayData.fromJson(json)
    }

    @Test
    @Description("minimal response with just orderId leaves the rest null")
    fun `minimal response`() {
        val json = JSONObject("""{"orderId":"o1"}""")

        val result = GPayData.fromJson(json)

        result.orderId shouldBe "o1"
        result.acsUrl shouldBe null
        result.paReq shouldBe null
        result.termUrl shouldBe null
        result.errorTypeName shouldBe null
        result.threeDSServerTransId shouldBe null
    }
}
