package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

@SmallTest
class BindingItemTest {

    private fun bindingItemJson(isMaestro: Boolean = false): JSONObject = JSONObject(
        """
        {
          "id": "binding-abc",
          "label": "654654***3843 12/24",
          "paymentSystem": "VISA",
          "cardHolder": "JOHN DOE",
          "createdDate": 1700000000,
          "payerEmail": "john@example.com",
          "payerPhone": "+79990001122",
          "isMaestro": $isMaestro
        }
        """.trimIndent()
    )

    @Test
    fun shouldParseAllFields() {
        val result = BindingItem.fromJson(bindingItemJson())

        result.id shouldBe "binding-abc"
        result.label shouldBe "654654***3843 12/24"
        result.paymentSystem shouldBe "VISA"
        result.cardHolder shouldBe "JOHN DOE"
        result.createdDate shouldBe 1700000000L
        result.payerEmail shouldBe "john@example.com"
        result.payerPhone shouldBe "+79990001122"
        result.isMaestro shouldBe false
    }

    @Test
    fun shouldParseMaestroFlagTrue() {
        val result = BindingItem.fromJson(bindingItemJson(isMaestro = true))

        result.isMaestro shouldBe true
    }

    @Test
    fun shouldParseMastercardPaymentSystem() {
        val json = JSONObject(
            """
            {
              "id": "b2",
              "label": "522222***4444 03/27",
              "paymentSystem": "MASTERCARD",
              "cardHolder": "JANE SMITH",
              "createdDate": 1710000000,
              "payerEmail": "jane@example.com",
              "payerPhone": "+79991234567",
              "isMaestro": false
            }
            """.trimIndent()
        )

        val result = BindingItem.fromJson(json)

        result.paymentSystem shouldBe "MASTERCARD"
        result.label shouldBe "522222***4444 03/27"
    }
}
