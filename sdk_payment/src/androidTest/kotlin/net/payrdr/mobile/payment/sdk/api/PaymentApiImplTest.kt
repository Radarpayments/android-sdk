package net.payrdr.mobile.payment.sdk.api

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.payment.model.ProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

@SmallTest
@Suppress("MaxLineLength")
@RunWith(AllureAndroidJUnit4::class)
class PaymentApiImplTest {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var server: MockWebServer = MockWebServer()
    private lateinit var paymentApiImpl: PaymentApi
    private lateinit var paymentToken: String
    private lateinit var mdOrder: String

    @Before
    fun setUp() {
        val localhostCertificate = HeldCertificate.Builder()
            .addSubjectAlternativeName("localhost")
            .build()

        val serverCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhostCertificate)
            .build()

        val clientCertificates = HandshakeCertificates.Builder()
            .addTrustedCertificate(localhostCertificate.certificate)
            .build()

        server = MockWebServer()
        server.useHttps(serverCertificates.sslSocketFactory(), false)

        HttpsURLConnection.setDefaultSSLSocketFactory(clientCertificates.sslSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier { _: String?, _: SSLSession? -> true }
        server.start()

        val baseUrl = "/payment"
        SDKPayment.init(
            SDKPaymentConfig(baseURL = baseUrl)
        )
        paymentApiImpl = PaymentApiImpl(
            server.url(baseUrl).toString()
        )
        paymentToken = ""
        mdOrder = "59b1ee2d-8353-7ab9-b3aa-73aa1917ef58"
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    @Description("shouldReturnSessionStatus")
    fun shouldReturnSessionStatus() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "remainingSecs":82981,
                        "orderNumber":"41420",
                        "amount":"20.00 RUB",
                        "bindingItems":[],
                        "bonusAmount":0,
                        "sslOnly":false,
                        "cvcNotRequired":false,
                        "otherWayEnabled":false,
                        "bindingEnabled":false,
                        "feeEnabled":false,
                        "loyaltyServices":[],
                        "bindingDeactivationEnabled":false,
                        "payerNotificationEnabled":false,
                        "merchantOptions":["AMEX","MASTERCARD","JCB","VISA","VISA_TDS","SSL","CUP"],
                        "customerDetails":{"email":"test@test.ru"},
                        "backUrl":"..\/merchants\/rbs\/finish.html?orderId=59b1ee2d-8353-7ab9-b3aa-73aa1917ef58&lang=ru",
                        "sessionParams":[],
                        "merchantInfo":{"merchantUrl":"http:\/\/hj.com","merchantFullName":"3ds2 for mobile SDK","merchantLogin":"3ds2","custom":false, "captchaMode": "NONE"},
                        "orderFeatures":[],
                        "orderExpired":false,
                        "orderPaymentWay":"CARD",
                        "expirationDateCustomValidation":false
                    }
                """.trimIndent()
            )
        )
        val sessionStatus = paymentApiImpl.getSessionStatus(mdOrder)

        assertEquals(82981L, sessionStatus.remainingSecs)
    }

    @Test
    @Description("shouldReturnProcessFormCardWithoutThreeDS")
    fun shouldReturnProcessFormCardWithoutThreeDS() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "redirect":"../merchants/rbs/finish.html?orderId=59881695-6fe4-747b-9b82-52a31917ef58&lang=ru",
                        "info":"Your payment has been processed and is being redirected...",
                        "errorCode":0
                    }
                """.trimIndent()
            )
        )
        val cryptogramApiData = ProcessFormRequest(
            paymentToken = paymentToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = false,
            email = null,
            mobilePhone = null,
            additionalPayerData = emptyMap()
        )
        val processForm = paymentApiImpl.processForm(cryptogramApiData)

        assertEquals(0, processForm.errorCode)
        assertEquals(null, processForm.threeDSServerTransId)
    }

    @Test
    @Description("shouldReturnBindingProcessForm")
    fun shouldReturnBindingProcessForm() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "errorCode": 1
                    }
                """.trimIndent()
            )
        )
        val cryptogramApiData = ProcessFormRequest(
            paymentToken = paymentToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = true,
            email = null,
            mobilePhone = null,
            additionalPayerData = emptyMap()
        )
        val processBindingForm = paymentApiImpl.processBindingForm(cryptogramApiData)

        assertEquals(1, processBindingForm.errorCode)
    }

    @Test
    @Description("shouldReturnSuccessFinishedPaymentInfo")
    fun shouldReturnSuccessFinishedPaymentInfo() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "paymentDate":"17.01.2021 17:44:36",
                        "status":"APPROVED"
                    }
                """.trimIndent()
            )
        )
        val finishedPaymentInfo = paymentApiImpl.getFinishedPaymentInfo(mdOrder)

        assertEquals("17.01.2021 17:44:36", finishedPaymentInfo.paymentDate)
        assertEquals("APPROVED", finishedPaymentInfo.status)
    }

    @Test
    @Description("shouldReturnNotSuccessFinishedPaymentInfo")
    fun shouldReturnNotSuccessFinishedPaymentInfo() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "paymentDate":"17.01.2021 17:44:36",
                        "status":"DECLINED"
                    }
                """.trimIndent()
            )
        )
        val finishedPaymentInfo = paymentApiImpl.getFinishedPaymentInfo(mdOrder)

        assertEquals("17.01.2021 17:44:36", finishedPaymentInfo.paymentDate)
        assertEquals("DECLINED", finishedPaymentInfo.status)
    }

    @Test(expected = SDKPaymentApiException::class)
    @Description("shouldReturnSessionStatusException")
    fun shouldReturnSessionStatusException() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "Incorrect body response"
                )
            )

            paymentApiImpl.getSessionStatus(mdOrder)
        }
    }

    @Test(expected = SDKPaymentApiException::class)
    @Description("shouldReturnProcessFormException")
    fun shouldReturnProcessFormException() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "Incorrect body response"
                )
            )
            val cryptogramApiData = ProcessFormRequest(
                paymentToken = paymentToken,
                mdOrder = mdOrder,
                holder = "CARDHOLDER",
                saveCard = false,
                email = null,
                mobilePhone = null,
                additionalPayerData = emptyMap()
            )
            paymentApiImpl.processForm(cryptogramApiData)
        }
    }

    @Test(expected = SDKPaymentApiException::class)
    @Description("shouldReturnProcessBindingFormException")
    fun shouldReturnProcessBindingFormException() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "Incorrect body response"
                )
            )
            val cryptogramApiData = ProcessFormRequest(
                paymentToken = paymentToken,
                mdOrder = mdOrder,
                holder = "CARDHOLDER",
                saveCard = false,
                email = null,
                mobilePhone = null,
                additionalPayerData = emptyMap()
            )
            paymentApiImpl.processBindingForm(cryptogramApiData)
        }
    }

    @Test(expected = SDKPaymentApiException::class)
    @Description("shouldReturnFinishPaymentInfoException")
    fun shouldReturnFinishPaymentInfoException() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "Incorrect body response"
                )
            )

            paymentApiImpl.getFinishedPaymentInfo(mdOrder)
        }
    }
}
