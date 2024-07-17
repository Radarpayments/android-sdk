package net.payrdr.mobile.payment.sdk.api

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.payment.model.ProcessFormRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@SmallTest
@Suppress("MaxLineLength")
@RunWith(AllureAndroidJUnit4::class)
class PaymentApiImplTest {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val server: MockWebServer = MockWebServer()
    private lateinit var paymentApiImpl: PaymentApi
    private lateinit var paymentToken: String
    private lateinit var mdOrder: String

    @Before
    fun setUp() {
        server.start()
        paymentApiImpl = PaymentApiImpl(
            server.url("/").toString()
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
                        "merchantInfo":{"merchantUrl":"http:\/\/hj.com","merchantFullName":"3ds2 for mobile SDK","merchantLogin":"3ds2","custom":false},
                        "orderFeatures":[],
                        "orderExpired":false,
                        "orderPaymentWay":"CARD",
                        "is3DSVer2":false,
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
                        "errorCode":0,
                        "is3DSVer2":false
                    }
                """.trimIndent()
            )
        )
        val cryptogramApiData = ProcessFormRequest(
            paymentToken = paymentToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = false
        )
        val processForm = paymentApiImpl.processForm(cryptogramApiData, true)

        assertEquals(0, processForm.errorCode)
        assertEquals(false, processForm.is3DSVer2)
        assertEquals(null, processForm.threeDSServerTransId)
    }

    @Test
    @Description("shouldReturnProcessFormCardWithThreeDS")
    fun shouldReturnProcessFormCardWithThreeDS() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "errorCode":0,
                        "is3DSVer2":true,
                        "threeDSServerTransId":"2def7b3a-caa3-42a4-97c4-f937a3d7c8cf",
                        "threeDSMethodURL":"https://dummy3ds.intabia.ru/acs2/acs/3dsMethod",
                        "threeDSMethodURLServer":"https://web.rbsdev.com/3dsserver/api/v1/client/gather?threeDSServerTransID=2def7b3a-caa3-42a4-97c4-f937a3d7c8cf",
                        "threeDSMethodDataPacked":"eyJ0aHJlZURTTWV0aG9kTm90aWZpY2F0aW9uVVJMIjoiaHR0cHM6Ly93ZWIucmJzZGV2LmNvbS8zZHNzZXJ2ZXIvYXBpL3YxL2Fjcy9ub3RpZmljYXRpb24_dGhyZWVEU1NlcnZlclRyYW5zSUQ9MmRlZjdiM2EtY2FhMy00MmE0LTk3YzQtZjkzN2EzZDdjOGNmIiwidGhyZWVEU1NlcnZlclRyYW5zSUQiOiIyZGVmN2IzYS1jYWEzLTQyYTQtOTdjNC1mOTM3YTNkN2M4Y2YifQ=="
                    }

                """.trimIndent()
            )
        )
        val cryptogramApiData = ProcessFormRequest(
            paymentToken = paymentToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = false
        )
        val processForm = paymentApiImpl.processForm(cryptogramApiData, true)

        assertEquals(0, processForm.errorCode)
        assertEquals(true, processForm.is3DSVer2)
        assertEquals("2def7b3a-caa3-42a4-97c4-f937a3d7c8cf", processForm.threeDSServerTransId)
    }

    @Test
    @Description("shouldReturnBindingProcessForm")
    fun shouldReturnBindingProcessForm() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "errorCode": 1,
                        "is3DSVer2": false
                    }
                """.trimIndent()
            )
        )
        val cryptogramApiData = ProcessFormRequest(
            paymentToken = paymentToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = true
        )
        val processBindingForm = paymentApiImpl.processBindingForm(cryptogramApiData, true)

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
                saveCard = false
            )
            paymentApiImpl.processForm(cryptogramApiData, true)
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
                saveCard = false
            )
            paymentApiImpl.processBindingForm(cryptogramApiData, true)
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
