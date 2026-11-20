package net.payrdr.mobile.payment.sdk.payment.model

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

@SmallTest
class GooglePayProcessFormRequestTest {

    @Test
    fun shouldPutPayerEmailAndPhoneIntoAdditionalParameters() {
        val json = GooglePayProcessFormRequest(
            paymentToken = "token",
            mdOrder = "order-1",
            email = "payer@example.com",
            phone = "+35799123456"
        ).toJson()

        json.getString("paymentToken") shouldBe "token"
        json.getString("mdOrder") shouldBe "order-1"
        val additionalParameters = json.getJSONObject("additionalParameters")
        additionalParameters.length() shouldBe 2
        additionalParameters.getString("email") shouldBe "payer@example.com"
        additionalParameters.getString("phone") shouldBe "+35799123456"
    }

    @Test
    fun shouldPutOnlyPresentPayerFields() {
        val additionalParameters = GooglePayProcessFormRequest(
            paymentToken = "token",
            mdOrder = "order-1",
            email = "payer@example.com"
        ).toJson().getJSONObject("additionalParameters")

        additionalParameters.length() shouldBe 1
        additionalParameters.getString("email") shouldBe "payer@example.com"
    }

    @Test
    fun shouldOmitAdditionalParametersWithoutPayerData() {
        val json = GooglePayProcessFormRequest(paymentToken = "token", mdOrder = "order-1").toJson()

        json.has("additionalParameters") shouldBe false
        json.length() shouldBe 2
    }

    @Test
    fun shouldEscapeSpecialCharacters() {
        val body = GooglePayProcessFormRequest(
            paymentToken = "to\"ken",
            mdOrder = "order-1"
        ).toJson().toString()

        JSONObject(body).getString("paymentToken") shouldBe "to\"ken"
    }
}
