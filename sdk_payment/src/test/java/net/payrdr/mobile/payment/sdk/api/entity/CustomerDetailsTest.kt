package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.json.JSONObject
import org.junit.Test

@SmallTest
class CustomerDetailsTest {

    @Test
    @Description("parses email and phone from JSON")
    fun `parses all fields`() {
        val json = JSONObject(
            """
            {
              "email": "user@example.com",
              "phone": "+79991234567"
            }
            """.trimIndent()
        )

        val result = CustomerDetails.fromJson(json)

        result.email shouldBe "user@example.com"
        result.phone shouldBe "+79991234567"
    }

    @Test
    @Description("missing keys become null")
    fun `missing fields default to null`() {
        val json = JSONObject("{}")

        val result = CustomerDetails.fromJson(json)

        result.email shouldBe null
        result.phone shouldBe null
    }

    @Test
    @Description("explicit null values become null")
    fun `explicit null values become null`() {
        val json = JSONObject(
            """
            {
              "email": null,
              "phone": null
            }
            """.trimIndent()
        )

        val result = CustomerDetails.fromJson(json)

        result.email shouldBe null
        result.phone shouldBe null
    }

    @Test
    @Description("null JSONObject argument returns all nulls")
    fun `null jsonObject argument`() {
        val result = CustomerDetails.fromJson(null)

        result.email shouldBe null
        result.phone shouldBe null
    }
}
