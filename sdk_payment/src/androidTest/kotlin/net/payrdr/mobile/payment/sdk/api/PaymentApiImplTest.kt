package net.payrdr.mobile.payment.sdk.api

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.payment.model.CryptogramApiData
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@SmallTest
@Suppress("MaxLineLength")
class PaymentApiImplTest {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val server: MockWebServer = MockWebServer()
    private lateinit var paymentApiImpl: PaymentApi
    private lateinit var seToken: String
    private lateinit var mdOrder: String

    @Before
    fun setUp() {
        server.start()
        paymentApiImpl = PaymentApiImpl(
            server.url("/").toString()
        )
        seToken = ""
        mdOrder = "59b1ee2d-8353-7ab9-b3aa-73aa1917ef58"
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
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
                        "merchantOptions":["AMEX","MASTERCARD","MIR","JCB","VISA","VISA_TDS","SSL","CUP"],
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
        val cryptogramApiData = CryptogramApiData(
            cryptogram = seToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = false
        )
        val processForm = paymentApiImpl.processForm(cryptogramApiData)

        assertEquals(0, processForm.errorCode)
        assertEquals(false, processForm.is3DSVer2)
        assertEquals(null, processForm.threeDSServerTransId)
    }

    @Test
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
        val cryptogramApiData = CryptogramApiData(
            cryptogram = seToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = false
        )
        val processForm = paymentApiImpl.processForm(cryptogramApiData)

        assertEquals(0, processForm.errorCode)
        assertEquals(true, processForm.is3DSVer2)
        assertEquals("2def7b3a-caa3-42a4-97c4-f937a3d7c8cf", processForm.threeDSServerTransId)
    }

    @Test
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
        val cryptogramApiData = CryptogramApiData(
            cryptogram = seToken,
            mdOrder = mdOrder,
            holder = "CARDHOLDER",
            saveCard = true
        )
        val processBindingForm = paymentApiImpl.processBindingForm(cryptogramApiData)

        assertEquals(1, processBindingForm.errorCode)
    }

    @Test
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
    fun shouldReturnProcessFormException() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "Incorrect body response"
                )
            )
            val cryptogramApiData = CryptogramApiData(
                cryptogram = seToken,
                mdOrder = mdOrder,
                holder = "CARDHOLDER",
                saveCard = false
            )
            paymentApiImpl.processForm(cryptogramApiData)
        }
    }

    @Test(expected = SDKPaymentApiException::class)
    fun shouldReturnProcessBindingFormException() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "Incorrect body response"
                )
            )
            val cryptogramApiData = CryptogramApiData(
                cryptogram = seToken,
                mdOrder = mdOrder,
                holder = "CARDHOLDER",
                saveCard = false
            )
            paymentApiImpl.processBindingForm(cryptogramApiData)
        }
    }

    @Test(expected = SDKPaymentApiException::class)
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
