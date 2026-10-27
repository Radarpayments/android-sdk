package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class ProcessFormResponseTest {

    @Test
    @Description("shouldReturnProcessFormObject")
    fun shouldReturnProcessFormObject() {
        val str = """
            {
                    "redirect":"../merchants/rbs/finish.html?orderId=59881695-6fe4-747b-9b82-52a31917ef58&lang=ru",
                    "info":"Your payment has been processed and is being redirected...",
                    "errorCode":0
            }
        """.trimIndent()

        val res = ProcessFormResponse.fromJson(jsonObject = JSONObject(str))

        assertEquals(0, res.errorCode)
    }

    @Test
    fun shouldParseResponseWithAcsUrl() {
        val json = JSONObject(
            """
            {
              "errorCode": 0,
              "acsUrl": "https://acs.example.com/challenge",
              "paReq": "test-pareq-value",
              "termUrl": "https://merchant.example.com/termination"
            }
            """.trimIndent()
        )

        val result = ProcessFormResponse.fromJson(json)

        result.errorCode shouldBe 0
        result.acsUrl shouldBe "https://acs.example.com/challenge"
        result.paReq shouldBe "test-pareq-value"
        result.termUrl shouldBe "https://merchant.example.com/termination"
        result.threeDSMethodURL shouldBe null
    }

    @Test
    fun shouldParseResponseWithThreeDSMethodURL() {
        val json = JSONObject(
            """
            {
              "errorCode": 0,
              "threeDSMethodURL": "https://3ds.example.com/method"
            }
            """.trimIndent()
        )

        val result = ProcessFormResponse.fromJson(json)

        result.errorCode shouldBe 0
        result.threeDSMethodURL shouldBe "https://3ds.example.com/method"
        result.acsUrl shouldBe null
    }

    @Test
    fun shouldParseResponseWithNonZeroErrorCode() {
        val json = JSONObject("""{"errorCode":2}""")

        val result = ProcessFormResponse.fromJson(json)

        result.errorCode shouldBe 2
        result.error shouldBe null
        result.acsUrl shouldBe null
        result.threeDSMethodURL shouldBe null
        result.paReq shouldBe null
        result.termUrl shouldBe null
    }

    @Test
    fun shouldParseErrorText() {
        val json = JSONObject(
            """{"errorCode":5,"error":"Invalid payer data: Mobile phone (Invalid value)"}"""
        )

        val result = ProcessFormResponse.fromJson(json)

        result.errorCode shouldBe 5
        result.error shouldBe "Invalid payer data: Mobile phone (Invalid value)"
    }

    @Test
    fun shouldParseResponseWithAllNullableFields() {
        val json = JSONObject("""{"errorCode":0}""")

        val result = ProcessFormResponse.fromJson(json)

        result.errorCode shouldBe 0
        result.acsUrl shouldBe null
        result.paReq shouldBe null
        result.termUrl shouldBe null
        result.threeDSMethodURL shouldBe null
    }
}
