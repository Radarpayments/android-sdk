package net.payrdr.mobile.payment.sdk.payment

import android.app.Activity
import android.content.Context
import android.util.Base64
import androidx.test.core.app.ActivityScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.PaymentConfigBuilder
import net.payrdr.mobile.payment.sdk.SDKForms
import net.payrdr.mobile.payment.sdk.SDKFormsConfigBuilder
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.component.impl.SSLContextConfig
import net.payrdr.mobile.payment.sdk.component.impl.SSLContextCustomCAFactory
import net.payrdr.mobile.payment.sdk.model.Card
import net.payrdr.mobile.payment.sdk.model.CardDeleteOptions
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.payment.model.PaymentApiVersion
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.test.core.ProcessDeath
import net.payrdr.mobile.payment.sdk.test.core.TestActivity
import net.payrdr.mobile.payment.sdk.ui.CardListActivity
import net.payrdr.mobile.payment.sdk.ui.CardNewActivity
import net.payrdr.mobile.payment.sdk.ui.PaymentBottomSheetFragment
import net.payrdr.mobile.payment.sdk.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.ui.helper.ThemeSetting
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

/**
 * MSDK-108: Android may kill the process while an SDK screen is in the back stack and later
 * recreate that screen in a fresh process, before the host app calls [SDKPayment.init] again.
 */
@RunWith(AllureAndroidJUnit4::class)
class ProcessDeathRecoveryTest {

    private val server = MockWebServer()
    private val serverCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .build()
    private val systemSocketFactory: SSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory()
    private val systemHostnameVerifier: HostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
    private val scenarios = mutableListOf<ActivityScenario<*>>()

    @Before
    fun setUp() {
        val serverCertificates = HandshakeCertificates.Builder()
            .heldCertificate(serverCertificate)
            .build()
        server.useHttps(serverCertificates.sslSocketFactory(), false)
        server.start()
    }

    @After
    fun tearDown() {
        scenarios.forEach { it.close() }
        server.shutdown()
        HttpsURLConnection.setDefaultSSLSocketFactory(systemSocketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier(systemHostnameVerifier)
        ProcessDeath.simulate()
        SDKPaymentConfigStorage.clear(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun shouldContinueCheckoutWhenPaymentActivityIsRecreatedAfterProcessDeath() {
        trustServerCertificateProcessWide()

        startCheckoutInPreviousProcess(SDKPaymentConfig(baseURL = baseUrl()))
        ProcessDeath.simulate()
        recreatePaymentActivity()

        val request = awaitRequest("Recreated PaymentActivity did not continue checkout")
        request.path shouldBe "/payment/rest/getSessionStatus.do"
        request.body.readUtf8() shouldContain MD_ORDER
    }

    @Test
    fun shouldKeepCustomCertificateWhenPaymentActivityIsRecreatedAfterProcessDeath() {
        val base64Certificate = Base64.encodeToString(
            serverCertificate.certificate.encoded,
            Base64.NO_WRAP,
        )
        val sslContextConfig = SSLContextCustomCAFactory.fromBase64String(base64Certificate)

        startCheckoutInPreviousProcess(
            SDKPaymentConfig(baseURL = baseUrl(), sslContextConfig = sslContextConfig)
        )
        ProcessDeath.simulate()
        recreatePaymentActivity()

        val request = awaitRequest("Recreated PaymentActivity lost the merchant CA certificate")
        request.path shouldBe "/payment/rest/getSessionStatus.do"
    }

    @Test
    fun shouldReturnErrorInsteadOfCrashWhenRecreatedPaymentActivityHasNoConfigToRestore() {
        // E.g. checkout was started by an older SDK version that did not persist the config.
        ProcessDeath.simulate()
        ApplicationProvider.getApplicationContext<Context>()
            .deleteSharedPreferences(SDKPaymentConfigStorage.PREFS_NAME)

        recreatePaymentActivity()

        val result = scenarios.last().result
        result.resultCode shouldBe Activity.RESULT_OK
        val resultData = result.resultData.apply {
            setExtrasClassLoader(PaymentResult::class.java.classLoader)
        }
        val paymentResult = requireNotNull(
            resultData.getParcelableExtra<PaymentResult>(INTENT_EXTRA_RESULT)
        )
        paymentResult.sessionId shouldBe MD_ORDER
        paymentResult.isSuccess shouldBe false
        requireNotNull(paymentResult.exception).message shouldContain "SDKPayment.init()"
        server.requestCount shouldBe 0
    }

    @Test
    fun shouldRestoreCardEntryScreenWhenItIsRecreatedAfterProcessDeath() {
        trustServerCertificateProcessWide()
        startCheckoutInPreviousProcess(SDKPaymentConfig(baseURL = baseUrl()))
        val config = PaymentConfigBuilder(MD_ORDER).build()
        ThemeSetting.setTheme(config.theme)
        LocalizationSetting.setLanguage(config.locale)
        val cardScreen = ActivityScenario.launch<CardNewActivity>(
            CardNewActivity.prepareIntent(ApplicationProvider.getApplicationContext(), config)
        )
        scenarios += cardScreen

        ProcessDeath.simulate()
        cardScreen.recreate()

        cardScreen.state shouldBe Lifecycle.State.RESUMED
    }

    @Test
    fun shouldNotStartCheckoutAgainWhenPaymentActivityIsRestoredFromSavedState() {
        trustServerCertificateProcessWide()
        server.dispatcher = gatewayDispatcher()
        val paymentScreen = launchPaymentActivityInPreviousProcess()
        awaitRequest("Checkout did not start").path shouldBe "/payment/rest/getSessionStatus.do"
        awaitBottomSheets(paymentScreen, expected = 1)

        ProcessDeath.simulate()
        paymentScreen.recreate()

        // Android restores the bottom sheet itself: no second checkout request and no second sheet.
        server.takeRequest(NO_REQUEST_WAIT_SEC, TimeUnit.SECONDS) shouldBe null
        awaitBottomSheets(paymentScreen, expected = 1)
    }

    @Test
    fun shouldReturnOrderStatusWhenRestoredPaymentActivityHasNothingToContinue() {
        trustServerCertificateProcessWide()
        val firstStatusRequest = java.util.concurrent.CountDownLatch(1)
        // The first getSessionStatus hangs: the process dies while the checkout request is in flight.
        server.dispatcher = gatewayDispatcher(hangFirstSessionStatus = firstStatusRequest)
        val paymentScreen = launchPaymentActivityInPreviousProcess(forResult = true)
        awaitRequest("Checkout did not start")

        ProcessDeath.simulate()
        paymentScreen.recreate()

        val result = paymentScreen.result
        firstStatusRequest.countDown()
        val paymentResult = requireNotNull(
            result.resultData.apply { setExtrasClassLoader(PaymentResult::class.java.classLoader) }
                .getParcelableExtra<PaymentResult>(INTENT_EXTRA_RESULT)
        )
        paymentResult.sessionId shouldBe MD_ORDER
        paymentResult.isSuccess shouldBe false
    }

    @Test
    fun shouldNotRestoreConfigWithBrokenCertificateAndClearIt() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences(SDKPaymentConfigStorage.PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("base_url", baseUrl())
            .putString("ca_certificate", "not a certificate")
            .commit()

        SDKPaymentConfigStorage.restore(context) shouldBe null
        context.getSharedPreferences(SDKPaymentConfigStorage.PREFS_NAME, Context.MODE_PRIVATE)
            .getString("base_url", null) shouldBe null
    }

    @Test
    fun shouldNotRestoreCustomSslContextWithoutCertificate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            .apply { init(null as java.security.KeyStore?) }
        val pinnedContext = SSLContext.getInstance("TLS").apply { init(null, trustManagerFactory.trustManagers, null) }
        val config = SDKPaymentConfig(
            baseURL = baseUrl(),
            sslContextConfig = SSLContextConfig(pinnedContext, trustManagerFactory.trustManagers.first()),
        )

        SDKPaymentConfigStorage.save(context, config)

        // Continuing without the merchant's SSLContext would silently drop pinning.
        SDKPaymentConfigStorage.restore(context) shouldBe null
    }

    @Test
    fun shouldKeepFormsConfigSetByHostWhenRestoringPaymentConfig() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        SDKPaymentConfigStorage.save(context, SDKPaymentConfig(baseURL = baseUrl()))
        ProcessDeath.simulate()
        val hostFormsConfig = SDKFormsConfigBuilder()
            .keyProvider(RemoteKeyProvider("https://host.example.com/se/keys.do"))
            .build()
        SDKForms.init(hostFormsConfig)

        SDKPayment.restoreConfigIfNeeded(context) shouldBe true

        (SDKForms.innerSdkConfig === hostFormsConfig) shouldBe true
    }

    @Test
    fun shouldUnbindCardFromCardListRecreatedAfterProcessDeath() {
        trustServerCertificateProcessWide()
        server.dispatcher = gatewayDispatcher()
        val context = ApplicationProvider.getApplicationContext<Context>()
        SDKPaymentConfigStorage.save(context, SDKPaymentConfig(baseURL = baseUrl()))
        val config = PaymentConfigBuilder(MD_ORDER)
            .cards(setOf(Card(pan = "411111**1111", bindingId = BINDING_ID)))
            .cardDeleteOptions(CardDeleteOptions.YES_DELETE)
            .build()
        // Recreated in a new process before the payment screen below it: no delete card handler is set.
        ProcessDeath.simulate()
        val cardList = ActivityScenario.launch<CardListActivity>(CardListActivity.prepareIntent(context, config))
        scenarios += cardList

        cardList.onActivity { activity ->
            CardListActivity::class.java.getDeclaredMethod("deleteCard", String::class.java)
                .apply { isAccessible = true }
                .invoke(activity, BINDING_ID)
        }

        val request = awaitRequest("Card was not unbound after process death")
        request.path shouldBe "/payment/rest/unbindcardanon.do"
        request.body.readUtf8() shouldContain BINDING_ID
    }

    private fun launchPaymentActivityInPreviousProcess(forResult: Boolean = false): ActivityScenario<PaymentActivity> {
        val config = SDKPaymentConfig(baseURL = baseUrl())
        SDKPayment.init(config)
        SDKPaymentConfigStorage.save(ApplicationProvider.getApplicationContext(), config)
        val intent = PaymentActivity.prepareIntent(
            ApplicationProvider.getApplicationContext(),
            MD_ORDER,
            false,
            PaymentApiVersion.V1,
        )
        val scenario = if (forResult) {
            ActivityScenario.launchActivityForResult<PaymentActivity>(intent)
        } else {
            ActivityScenario.launch(intent)
        }
        scenarios += scenario
        return scenario
    }

    private fun awaitBottomSheets(scenario: ActivityScenario<PaymentActivity>, expected: Int) {
        val deadline = System.currentTimeMillis() + REQUEST_TIMEOUT_SEC * 1000
        var count = -1
        while (System.currentTimeMillis() < deadline) {
            scenario.onActivity { activity ->
                count = activity.supportFragmentManager.fragments.count { it is PaymentBottomSheetFragment }
            }
            if (count == expected) return
            Thread.sleep(POLL_MS)
        }
        count shouldBe expected
    }

    /** Answers like the real gateway (responses captured from dev.bpcbt.com for an unpaid order). */
    private fun gatewayDispatcher(hangFirstSessionStatus: java.util.concurrent.CountDownLatch? = null) =
        object : Dispatcher() {
            private var sessionStatusRequests = 0

            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path.orEmpty().endsWith("/rest/getSessionStatus.do") -> {
                    if (sessionStatusRequests++ == 0 && hangFirstSessionStatus != null) {
                        hangFirstSessionStatus.await(REQUEST_TIMEOUT_SEC * 3, TimeUnit.SECONDS)
                    }
                    MockResponse().setBody(SESSION_STATUS_JSON)
                }
                request.path.orEmpty().endsWith("/rest/getFinishedPaymentInfo.do") ->
                    MockResponse().setBody(FINISHED_PAYMENT_INFO_JSON)
                request.path.orEmpty().endsWith("/rest/unbindcardanon.do") ->
                    MockResponse().setBody("""{"errorCode":0}""")
                else -> MockResponse().setResponseCode(404)
            }
        }

    private fun baseUrl() = server.url("/payment").toString().removeSuffix("/")

    private fun trustServerCertificateProcessWide() {
        val clientCertificates = HandshakeCertificates.Builder()
            .addTrustedCertificate(serverCertificate.certificate)
            .build()
        HttpsURLConnection.setDefaultSSLSocketFactory(clientCertificates.sslSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    }

    private fun startCheckoutInPreviousProcess(config: SDKPaymentConfig) {
        SDKPayment.init(config)
        scenarios += ActivityScenario.launch(TestActivity::class.java).onActivity { host ->
            SDKPayment.checkout(host, CheckoutConfig.MdOrder(MD_ORDER))
        }
        awaitRequest("Checkout in the previous process did not reach the server")
    }

    /** Android recreates the top activity from its saved intent in the new process. */
    private fun recreatePaymentActivity() {
        val savedIntent = PaymentActivity.prepareIntent(
            ApplicationProvider.getApplicationContext(),
            MD_ORDER,
            false,
            PaymentApiVersion.V1,
        )
        scenarios += ActivityScenario.launchActivityForResult<PaymentActivity>(savedIntent)
    }

    private fun awaitRequest(failureMessage: String): RecordedRequest =
        requireNotNull(server.takeRequest(REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS)) { failureMessage }

    private companion object {
        const val MD_ORDER = "11111111-2222-3333-4444-555555555555"
        const val BINDING_ID = "b4369c6a-0557-7ab8-93e7-527600aaf1bc"
        const val REQUEST_TIMEOUT_SEC = 10L
        const val NO_REQUEST_WAIT_SEC = 3L
        const val POLL_MS = 100L

        const val SESSION_STATUS_JSON = """{"remainingSecs":1199,"orderNumber":"5148","amount":"1.00 EUR",""" +
            """"paymentState":"started","bindingItems":[],"sslOnly":false,"cvcNotRequired":false,""" +
            """"otherWayEnabled":false,"bindingEnabled":false,"feeEnabled":false,""" +
            """"bindingDeactivationEnabled":true,"payerNotificationEnabled":false,""" +
            """"merchantOptions":["MASTERCARD","VISA","SSL","CARD"],"customerDetails":{"suggestedCountryCodes":[]},""" +
            """"backUrl":"sdk://done","sessionParams":[],"merchantInfo":{"merchantUrl":"https://example.com",""" +
            """"merchantFullName":"test-sandbox","mcc":"0000","merchantLogin":"test-sandbox",""" +
            """"merchantCountry":{"countryCode":"643","countryCodeAlpha2":"RU"},"captchaMode":"NONE",""" +
            """"loadedResources":{"logo":false,"footer":false},"custom":false},"orderFeatures":[],""" +
            """"orderExpired":false,"orderPaymentWay":"UNKNOWN","is3DSVer2":false,""" +
            """"expirationDateCustomValidation":false,"currencyAlphaCode":"EUR","currencyNumericCode":"978",""" +
            """"authType":"PURCHASE","payerDataParamsNeedToBeFilled":{},"environment":"TEST","isWebView":false}"""

        const val FINISHED_PAYMENT_INFO_JSON = """{"merchantShortName":"test-sandbox","orderNumber":"5148",""" +
            """"actionCode":-100,"actionCodeDescription":"Operation pending or in progress.",""" +
            """"formattedAmount":"1.00","paymentDate":"15.10.2026 16:26:01","terminalId":"31971540",""" +
            """"approvalCode":"null","refNum":"null","panMasked4digits":"**** **** **** ",""" +
            """"errorTypeName":"NO_PAYMENT_ATTEMPT","status":"CREATED"}"""
    }
}
