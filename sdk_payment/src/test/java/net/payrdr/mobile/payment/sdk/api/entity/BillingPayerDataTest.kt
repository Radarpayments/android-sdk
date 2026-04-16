package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.json.JSONObject
import org.junit.Test

@SmallTest
class BillingPayerDataTest {

    @Test
    @Description("parses all billing address fields")
    fun `parses all fields`() {
        val json = JSONObject(
            """
            {
              "billingAddressLine1": "Line1",
              "billingAddressLine2": "Line2",
              "billingAddressLine3": "Line3",
              "billingCity": "Moscow",
              "billingCountry": "RU",
              "billingPostalCode": "123456",
              "billingState": "Moskovskaya"
            }
            """.trimIndent()
        )

        val result = BillingPayerData.fromJson(json)

        result.billingAddressLine1 shouldBe "Line1"
        result.billingAddressLine2 shouldBe "Line2"
        result.billingAddressLine3 shouldBe "Line3"
        result.billingCity shouldBe "Moscow"
        result.billingCountry shouldBe "RU"
        result.billingPostalCode shouldBe "123456"
        result.billingState shouldBe "Moskovskaya"
    }

    @Test
    @Description("null input produces all-null data")
    fun `null jsonObject produces nulls`() {
        val result = BillingPayerData.fromJson(null)

        result.billingAddressLine1 shouldBe null
        result.billingCity shouldBe null
        result.billingCountry shouldBe null
        result.billingPostalCode shouldBe null
        result.billingState shouldBe null
    }

    @Test
    @Description("partial JSON leaves omitted fields null")
    fun `partial JSON leaves omitted fields null`() {
        val json = JSONObject(
            """
            {
              "billingCity": "Moscow",
              "billingCountry": "RU"
            }
            """.trimIndent()
        )

        val result = BillingPayerData.fromJson(json)

        result.billingCity shouldBe "Moscow"
        result.billingCountry shouldBe "RU"
        result.billingAddressLine1 shouldBe null
        result.billingAddressLine2 shouldBe null
        result.billingAddressLine3 shouldBe null
        result.billingState shouldBe null
        result.billingPostalCode shouldBe null
    }
}
