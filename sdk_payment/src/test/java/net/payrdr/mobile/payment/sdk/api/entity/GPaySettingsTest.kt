package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test

@SmallTest
class GPaySettingsTest {

    @Test
    @Description("parses GPay settings from nested settings object")
    fun `parses nested settings block`() {
        val json = JSONObject(
            """
            {
              "settings": {
                "googlePay.environment": "TEST",
                "googlePay.merchantId": "merchant-123",
                "googlePay.gateway": "gateway-xyz"
              }
            }
            """.trimIndent()
        )

        val result = GPaySettings.fromJson(json)

        result.environment shouldBe "TEST"
        result.merchantId shouldBe "merchant-123"
        result.gateway shouldBe "gateway-xyz"
    }

    @Test(expected = JSONException::class)
    @Description("missing settings block throws JSONException")
    fun `missing settings block throws`() {
        val json = JSONObject("{}")
        GPaySettings.fromJson(json)
    }

    @Test(expected = JSONException::class)
    @Description("missing googlePay fields within settings throw")
    fun `missing inner field throws`() {
        val json = JSONObject(
            """{"settings":{"googlePay.environment":"TEST"}}"""
        )
        GPaySettings.fromJson(json)
    }
}
