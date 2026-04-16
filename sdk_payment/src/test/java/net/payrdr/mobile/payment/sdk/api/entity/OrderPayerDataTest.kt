package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.json.JSONObject
import org.junit.Test

@SmallTest
class OrderPayerDataTest {

    @Test
    @Description("reads mobilePhone from JSON")
    fun `parses mobile phone`() {
        val json = JSONObject("""{"mobilePhone":"+79991234567"}""")

        val result = OrderPayerData.fromJson(json)

        result.mobilePhone shouldBe "+79991234567"
    }

    @Test
    @Description("optString returns empty string when field is missing")
    fun `missing key returns empty string`() {
        val json = JSONObject("{}")

        val result = OrderPayerData.fromJson(json)

        result.mobilePhone shouldBe ""
    }

    @Test
    @Description("null jsonObject returns null mobilePhone")
    fun `null jsonObject returns null`() {
        val result = OrderPayerData.fromJson(null)

        result.mobilePhone shouldBe null
    }
}
