package net.payrdr.mobile.payment.sdk.payment

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.utils.toExpDate
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.SDKForms.cryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.utils.executePostParams
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.net.URL

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class InstancePaymentTest {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setUp() {
        SDKForms.init(
            SDKConfigBuilder()
                .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
                .build()
        )
    }

    @Test
    @Description("shouldReturnSuccessPaymentWithoutOrder")
    fun shouldReturnSuccessPaymentWithoutOrder(): Unit = runBlocking {
        val config = PaymentConfigTestProvider.defaultConfig()
        val seToken = cryptogramProcessor.create(
            uuid = config.uuid,
            timestamp = config.timestamp,
            cardInfo = CardInfo(
                identifier = CardPanIdentifier(
                    "5000001111111115"
                ),
                expDate = "12/30".toExpDate(),
                cvv = "123"
            )
        )

        val response = makeInstancePayment(seToken)

        assertEquals("Success", response)
    }

    private fun makeInstancePayment(seToken: String): String? {
        val url = "https://dev.bpcbt.com/payment/rest/instantPayment.do"
        val body = mapOf(
            "amount" to "20000",
            "userName" to "mobile-sdk-api",
            "password" to "vkyvbG0",
            "currency" to "643",
            "seToken" to seToken,
            "cardHolderName" to "CARD HOLDER",
            "backUrl" to "https://thebestmerchanturl.com",
        )

        return runCatching {
            val connection = URL(url).executePostParams(body)
            val response = connection.responseBodyToJsonObject()
            response.getJSONObject("orderStatus").getString("ErrorMessage")
        }.getOrNull()
    }
}
