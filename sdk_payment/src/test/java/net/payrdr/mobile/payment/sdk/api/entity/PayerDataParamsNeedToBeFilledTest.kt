package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.json.JSONObject
import org.junit.Test

@SmallTest
class PayerDataParamsNeedToBeFilledTest {

    @Test
    @Description("parses MASTERCARD and VISA field lists")
    fun `parses both card system lists`() {
        val json = JSONObject(
            """
            {
              "MASTERCARD": [
                {"mandatory": true, "name": "email"},
                {"mandatory": false, "name": "phone"}
              ],
              "VISA": [
                {"mandatory": true, "name": "billingCountry"}
              ]
            }
            """.trimIndent()
        )

        val result = PayerDataParamsNeedToBeFilled.fromJson(json)

        result.mastercard?.size shouldBe 2
        result.mastercard?.get(0) shouldBe FieldParams(isMandatory = true, name = "email")
        result.mastercard?.get(1) shouldBe FieldParams(isMandatory = false, name = "phone")
        result.visa?.size shouldBe 1
        result.visa?.get(0) shouldBe FieldParams(isMandatory = true, name = "billingCountry")
    }

    @Test
    @Description("empty arrays produce empty lists, not nulls")
    fun `empty arrays produce empty lists`() {
        val json = JSONObject("""{"MASTERCARD":[],"VISA":[]}""")

        val result = PayerDataParamsNeedToBeFilled.fromJson(json)

        result.mastercard shouldBe emptyList()
        result.visa shouldBe emptyList()
    }

    @Test
    @Description("missing arrays become null")
    fun `missing arrays become null`() {
        val json = JSONObject("{}")

        val result = PayerDataParamsNeedToBeFilled.fromJson(json)

        result.mastercard shouldBe null
        result.visa shouldBe null
    }

    @Test
    @Description("null jsonObject returns all-null result")
    fun `null jsonObject input`() {
        val result = PayerDataParamsNeedToBeFilled.fromJson(null)

        result.mastercard shouldBe null
        result.visa shouldBe null
    }

    @Test
    @Description("only one card system present leaves the other null")
    fun `only visa present`() {
        val json = JSONObject(
            """{"VISA":[{"mandatory":false,"name":"email"}]}"""
        )

        val result = PayerDataParamsNeedToBeFilled.fromJson(json)

        result.mastercard shouldBe null
        result.visa?.size shouldBe 1
    }
}
