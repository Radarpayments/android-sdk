package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

@SmallTest
class SessionStatusResponseTest {

    private fun minimalJson(extra: String = ""): JSONObject = JSONObject(
        """
        {
          "remainingSecs": 60,
          "cvcNotRequired": false,
          "otherWayEnabled": false,
          "feeEnabled": false,
          "orderExpired": false,
          "expirationDateCustomValidation": false,
          "bindingItems": [],
          "merchantInfo": {
            "merchantUrl": "https://merchant.example.com",
            "merchantFullName": "Test Merchant",
            "merchantLogin": "test_login",
            "captchaMode": "NONE",
            "custom": false
          },
          "merchantOptions": []
          ${if (extra.isNotEmpty()) ",$extra" else ""}
        }
        """.trimIndent()
    )

    @Test
    fun shouldParseRemainingSecsAndEmptyBindings() {
        val result = SessionStatusResponse.fromJson(minimalJson())

        result.remainingSecs shouldBe 60L
        result.bindingItems!!.isEmpty() shouldBe true
        result.cvcNotRequired shouldBe false
        result.merchantInfo.merchantFullName shouldBe "Test Merchant"
        result.merchantOptions.isEmpty() shouldBe true
    }

    @Test
    fun shouldParseRemainingSecsWhenAbsent() {
        val json = JSONObject(
            """
            {
              "cvcNotRequired": false, "otherWayEnabled": false, "feeEnabled": false,
              "orderExpired": false, "expirationDateCustomValidation": false,
              "bindingItems": [],
              "merchantInfo": {
                "merchantUrl": "https://m.com", "merchantFullName": "M",
                "merchantLogin": "login", "captchaMode": "NONE", "custom": false
              },
              "merchantOptions": []
            }
            """.trimIndent()
        )

        val result = SessionStatusResponse.fromJson(json)

        result.remainingSecs shouldBe null
    }

    @Test
    fun shouldParseBindingItems() {
        val jsonWithBinding = JSONObject(
            """
            {
              "remainingSecs": 60,
              "cvcNotRequired": false, "otherWayEnabled": false, "feeEnabled": false,
              "orderExpired": false, "expirationDateCustomValidation": false,
              "bindingItems": [{
                "id": "binding-1",
                "label": "654654***3843 12/24",
                "paymentSystem": "VISA",
                "cardHolder": "JOHN DOE",
                "createdDate": 1700000000,
                "payerEmail": "test@test.com",
                "payerPhone": "+79990000000",
                "isMaestro": false
              }],
              "merchantInfo": {
                "merchantUrl": "https://merchant.example.com",
                "merchantFullName": "Test Merchant",
                "merchantLogin": "test_login",
                "captchaMode": "NONE",
                "custom": false
              },
              "merchantOptions": []
            }
            """.trimIndent()
        )

        val result = SessionStatusResponse.fromJson(jsonWithBinding)

        result.bindingItems!!.size shouldBe 1
        result.bindingItems!![0].id shouldBe "binding-1"
        result.bindingItems!![0].paymentSystem shouldBe "VISA"
    }

    @Test
    fun shouldParseGooglePayMerchantOption() {
        val json = JSONObject(
            """
            {
              "remainingSecs": 120,
              "cvcNotRequired": false, "otherWayEnabled": false, "feeEnabled": false,
              "orderExpired": false, "expirationDateCustomValidation": false,
              "bindingItems": [],
              "merchantInfo": {
                "merchantUrl": "https://m.com", "merchantFullName": "M",
                "merchantLogin": "login", "captchaMode": "NONE", "custom": false
              },
              "merchantOptions": ["GOOGLEPAY", "CARDS"],
              "amount": "100.00",
              "currencyAlphaCode": "RUB"
            }
            """.trimIndent()
        )

        val result = SessionStatusResponse.fromJson(json)

        result.merchantOptions.contains("GOOGLEPAY") shouldBe true
        result.amount shouldBe "100.00"
        result.currencyAlphaCode shouldBe "RUB"
    }

    @Test
    fun shouldParsePayerDataParams() {
        val json = JSONObject(
            """
            {
              "remainingSecs": 60,
              "cvcNotRequired": false, "otherWayEnabled": false, "feeEnabled": false,
              "orderExpired": false, "expirationDateCustomValidation": false,
              "bindingItems": [],
              "merchantInfo": {
                "merchantUrl": "https://m.com", "merchantFullName": "M",
                "merchantLogin": "login", "captchaMode": "NONE", "custom": false
              },
              "merchantOptions": [],
              "payerDataParamsNeedToBeFilled": {
                "VISA": [{"mandatory": true, "name": "EMAIL"}],
                "MASTERCARD": [{"mandatory": false, "name": "MOBILE_PHONE"}]
              }
            }
            """.trimIndent()
        )

        val result = SessionStatusResponse.fromJson(json)

        result.payerDataParamsNeedToBeFilled.visa!!.size shouldBe 1
        result.payerDataParamsNeedToBeFilled.visa!![0].name shouldBe "EMAIL"
        result.payerDataParamsNeedToBeFilled.visa!![0].isMandatory shouldBe true
        result.payerDataParamsNeedToBeFilled.mastercard!!.size shouldBe 1
        result.payerDataParamsNeedToBeFilled.mastercard!![0].name shouldBe "MOBILE_PHONE"
        result.payerDataParamsNeedToBeFilled.mastercard!![0].isMandatory shouldBe false
    }

    @Test
    fun shouldParseCustomerDetails() {
        val json = JSONObject(
            """
            {
              "remainingSecs": 60,
              "cvcNotRequired": false, "otherWayEnabled": false, "feeEnabled": false,
              "orderExpired": false, "expirationDateCustomValidation": false,
              "bindingItems": [],
              "merchantInfo": {
                "merchantUrl": "https://m.com", "merchantFullName": "M",
                "merchantLogin": "login", "captchaMode": "NONE", "custom": false
              },
              "merchantOptions": [],
              "customerDetails": {"email": "user@example.com", "phone": "+70000000000"}
            }
            """.trimIndent()
        )

        val result = SessionStatusResponse.fromJson(json)

        result.customerDetails.email shouldBe "user@example.com"
        result.customerDetails.phone shouldBe "+70000000000"
    }

    @Test
    fun shouldParseBillingPayerData() {
        val json = JSONObject(
            """
            {
              "remainingSecs": 60,
              "cvcNotRequired": false, "otherWayEnabled": false, "feeEnabled": false,
              "orderExpired": false, "expirationDateCustomValidation": false,
              "bindingItems": [],
              "merchantInfo": {
                "merchantUrl": "https://m.com", "merchantFullName": "M",
                "merchantLogin": "login", "captchaMode": "NONE", "custom": false
              },
              "merchantOptions": [],
              "billingPayerData": {
                "billingCity": "Moscow",
                "billingCountry": "RU",
                "billingPostalCode": "123456",
                "billingAddressLine1": "Street 1"
              }
            }
            """.trimIndent()
        )

        val result = SessionStatusResponse.fromJson(json)

        result.billingPayerData.billingCity shouldBe "Moscow"
        result.billingPayerData.billingCountry shouldBe "RU"
        result.billingPayerData.billingPostalCode shouldBe "123456"
        result.billingPayerData.billingAddressLine1 shouldBe "Street 1"
    }

    @Test
    fun shouldParseNullableBillingAndCustomerDetailsWhenAbsent() {
        val result = SessionStatusResponse.fromJson(minimalJson())

        result.billingPayerData.billingCity shouldBe null
        result.customerDetails.email shouldBe null
        result.orderPayerData.mobilePhone shouldBe null
    }
}
