package net.payrdr.mobile.payment.sdk.data

import android.content.Context
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.PaymentApiImpl
import net.payrdr.mobile.payment.sdk.api.entity.SessionStatusResponse
import net.payrdr.mobile.payment.sdk.core.SDKCore
import net.payrdr.mobile.payment.sdk.core.model.CardParams
import net.payrdr.mobile.payment.sdk.core.model.SDKCoreConfig
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.utils.executePostParams
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import net.payrdr.mobile.payment.sdk.payment.model.ProcessFormRequest
import net.payrdr.mobile.payment.sdk.utils.executePostJsonForSessionId
import org.json.JSONObject
import java.net.URL

class TestOrderHelper(
    private val context: Context,
    private val baseUrl: String,
) {

    private var paymentApi: PaymentApi = PaymentApiImpl(baseUrl)

    suspend fun preparePayedOrder(): String {
        val orderId = registerOrder()

        val card = TestCardHelper.cardSuccessSSL

        val pubKey = RemoteKeyProvider(url = "${baseUrl}/se/keys.do")
            .provideKey().value

        val params = CardParams(
            mdOrder = orderId,
            pan = card.pan,
            cvc = card.cvc,
            cardHolder = card.holder,
            expiryMMYY = card.expiry,
            pubKey = pubKey
        )

        val paymentToken = SDKCore(context).generateWithConfig(SDKCoreConfig(params)).token
            ?: throw IllegalStateException()

        val cryptogramApiData = ProcessFormRequest(
            paymentToken = paymentToken,
            mdOrder = orderId,
            holder = card.holder,
            saveCard = false,
        )
        paymentApi.processForm(
            cryptogramApiData = cryptogramApiData,
            threeDSSDK = false,
        )
        paymentApi.getFinishedPaymentInfo(
            orderId = orderId,
        ).status shouldBe "DEPOSITED"

        return orderId
    }

    fun registerOrder(
        amount: Int = 20000,
        returnUrl: String = "sdk://done",
        userName: String = "mobile-sdk-api",
        password: String = "vkyvbG0",
        clientId: String? = null,
    ): String {
        val url = "${baseUrl}/rest/register.do"
        val body = mutableMapOf<String, String>()
        body["amount"] = amount.toString()
        body["userName"] = userName
        body["password"] = password
        body["returnUrl"] = returnUrl
        if (clientId != null) {
            body["clientId"] = clientId
        }

        return runCatching {
            val connection = URL(url).executePostParams(body.toMap())
            connection.responseBodyToJsonObject().getString("orderId")
        }.getOrNull() ?: throw IllegalStateException("Could not register order")
    }

    fun registerSession(
        amount: Int = 100,
        currency: String = "USD",
        resultUrl: String = "sdk://done",
        apiKey: String = "9yVrffWNAiHUUVUCQoX4NFHMxmRHYA2yB",
        version: String = "2023-10-31"

    ): String {
        val apiUrl = "https://dev.bpcbt.com/api2/sessions"
        val body = mutableMapOf<String, Any>()
        body["amount"] = amount
        body["currency"] = currency
        body["successUrl"] = resultUrl
        body["failureUrl"] = resultUrl
        val json = JSONObject(body.toMap())

        return runCatching {
            val connection = URL(apiUrl).executePostJsonForSessionId(json.toString(), apiKey, version)
            connection.responseBodyToJsonObject().getString("id")
        }.getOrNull() ?: throw IllegalStateException("Could not register session")
    }

    suspend fun getSessionStatus(mdOrder: String): SessionStatusResponse {
        return paymentApi.getSessionStatus(mdOrder)
    }

}
