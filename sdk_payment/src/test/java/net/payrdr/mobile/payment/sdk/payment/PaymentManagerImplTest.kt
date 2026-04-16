package net.payrdr.mobile.payment.sdk.payment

import androidx.test.filters.MediumTest
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.entity.BillingPayerData
import net.payrdr.mobile.payment.sdk.api.entity.BindingItem
import net.payrdr.mobile.payment.sdk.api.entity.CustomerDetails
import net.payrdr.mobile.payment.sdk.api.entity.FinishedPaymentInfoResponse
import net.payrdr.mobile.payment.sdk.api.entity.MerchantInfo
import net.payrdr.mobile.payment.sdk.api.entity.OrderPayerData
import net.payrdr.mobile.payment.sdk.api.entity.PayerDataParamsNeedToBeFilled
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormGPayResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormResponse
import net.payrdr.mobile.payment.sdk.api.entity.SessionStatusResponse
import net.payrdr.mobile.payment.sdk.exceptions.SDKAlreadyPaymentException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKNotConfigureException
import net.payrdr.mobile.payment.sdk.model.FilledAdditionalPayerParams
import net.payrdr.mobile.payment.sdk.payment.model.GooglePayProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.PaymentApiVersion
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@MediumTest
class PaymentManagerImplTest {

    private lateinit var cardFormDelegate: CardFormDelegate
    private lateinit var threeDS2WebFormDelegate: ThreeDS2WebFormDelegate
    private lateinit var activityDelegate: ActivityDelegate
    private lateinit var gPayDelegate: GPayDelegate
    private lateinit var mockApi: PaymentApi
    private lateinit var paymentManager: PaymentManagerImpl

    @Before
    fun setUp() {
        mockApi = mockk(relaxed = true)
        cardFormDelegate = mockk(relaxed = true)
        threeDS2WebFormDelegate = mockk(relaxed = true)
        activityDelegate = mockk(relaxed = true)
        gPayDelegate = mockk(relaxed = true)
        every { activityDelegate.getPaymentConfig() } returns
            SDKPaymentConfig(baseURL = "https://test.example.com")

        paymentManager = PaymentManagerImpl(
            cardFormDelegate, threeDS2WebFormDelegate, activityDelegate, gPayDelegate
        )

        // Inject mock API via reflection to bypass internal PaymentApiImpl construction
        val apiField = PaymentManagerImpl::class.java.getDeclaredField("paymentApi")
        apiField.isAccessible = true
        apiField.set(paymentManager, mockApi)
    }

    // ---- helpers ----

    private fun minimalSessionStatus(
        remainingSecs: Long? = 60L,
        bindingItems: List<BindingItem>? = null
    ): SessionStatusResponse = SessionStatusResponse(
        remainingSecs = remainingSecs,
        cvcNotRequired = false,
        otherWayEnabled = false,
        feeEnabled = false,
        orderExpired = false,
        expirationDateCustomValidation = false,
        merchantInfo = MerchantInfo(
            merchantUrl = "https://m.com",
            merchantFullName = "Merchant",
            merchantLogin = "login",
            captchaMode = "NONE",
            custom = false
        ),
        merchantOptions = emptyList(),
        orderPayerData = OrderPayerData(mobilePhone = null),
        customerDetails = CustomerDetails(email = null, phone = null),
        billingPayerData = BillingPayerData(null, null, null, null, null, null, null),
        payerDataParamsNeedToBeFilled = PayerDataParamsNeedToBeFilled(mastercard = null, visa = null),
        bindingItems = bindingItems
    )

    private fun declinedPaymentInfo() = FinishedPaymentInfoResponse(
        status = "DECLINED",
        paymentDate = null, approvalCode = null, terminalId = null,
        refNum = null, panMasked = null, formattedAmount = null, actionCodeDescription = null
    )

    private fun depositedPaymentInfo() = FinishedPaymentInfoResponse(
        status = "DEPOSITED",
        paymentDate = null, approvalCode = null, terminalId = null,
        refNum = null, panMasked = null, formattedAmount = null, actionCodeDescription = null
    )

    private fun emptyFilledParams() =
        FilledAdditionalPayerParams(null, null, null, null, null, null, null, null, null)

    /**
     * Calls checkout() and waits until cardFormDelegate.openBottomSheet() is invoked.
     * Returns true if the latch counted down in time.
     */
    private fun doCheckout(order: String = "order-123"): Boolean {
        coEvery { mockApi.getSessionStatus(any()) } returns minimalSessionStatus()
        val latch = CountDownLatch(1)
        every { cardFormDelegate.openBottomSheet(any(), any(), any(), any(), any(), any(), any()) } answers {
            latch.countDown()
        }
        paymentManager.checkout(order, gPayClicked = false, versionApi = PaymentApiVersion.V1)
        return latch.await(3, TimeUnit.SECONDS)
    }

    // ---- checkout() routing ----

    @Test
    fun checkoutWithNoBindingItemsOpensNewCardBottomSheet() {
        coEvery { mockApi.getSessionStatus(any()) } returns minimalSessionStatus(bindingItems = null)

        val latch = CountDownLatch(1)
        every { cardFormDelegate.openBottomSheet(any(), any(), any(), any(), any(), any(), any()) } answers {
            latch.countDown()
        }

        paymentManager.checkout("order-1", gPayClicked = false, versionApi = PaymentApiVersion.V1)

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        verify {
            cardFormDelegate.openBottomSheet(
                mdOrder = "order-1",
                bindingEnabled = false,
                bindingCards = emptyList(),
                cvcNotRequired = false,
                bindingDeactivationEnabled = false,
                googlePayConfig = null,
                additionalCardParamForPayments = any()
            )
        }
    }

    @Test
    fun checkoutWithBindingItemsOpensCardListBottomSheet() {
        val binding = BindingItem(
            id = "b-1", label = "654654***3843 12/24", paymentSystem = "VISA",
            cardHolder = "JOHN DOE", createdDate = 1700000000L,
            payerEmail = "t@t.com", payerPhone = "+79990000000", isMaestro = false
        )
        coEvery { mockApi.getSessionStatus(any()) } returns
            minimalSessionStatus(bindingItems = listOf(binding))

        val latch = CountDownLatch(1)
        every { cardFormDelegate.openBottomSheet(any(), any(), any(), any(), any(), any(), any()) } answers {
            latch.countDown()
        }

        paymentManager.checkout("order-2", gPayClicked = false, versionApi = PaymentApiVersion.V1)

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        verify {
            cardFormDelegate.openBottomSheet(
                mdOrder = "order-2",
                bindingEnabled = false,
                bindingCards = listOf(binding),
                cvcNotRequired = false,
                bindingDeactivationEnabled = false,
                googlePayConfig = null,
                additionalCardParamForPayments = any()
            )
        }
    }

    @Test
    fun checkoutAlreadyPaidOrderFinishesWithAlreadyPaymentException() {
        coEvery { mockApi.getSessionStatus(any()) } returns minimalSessionStatus(remainingSecs = null)
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns depositedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.checkout("order-3", gPayClicked = false, versionApi = PaymentApiVersion.V1)

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        (capturedResult!!.exception is SDKAlreadyPaymentException) shouldBe true
        capturedResult!!.sessionId shouldBe "order-3"
    }

    @Test
    fun checkoutDeclinedOrderFinishesWithDeclinedException() {
        coEvery { mockApi.getSessionStatus(any()) } returns minimalSessionStatus(remainingSecs = null)
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns declinedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.checkout("order-4", gPayClicked = false, versionApi = PaymentApiVersion.V1)

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        (capturedResult!!.exception is SDKDeclinedException) shouldBe true
    }

    // ---- processNewCard() / processFormData() routing ----

    @Test
    fun processNewCardWithAcsUrlOpensWebChallenge() {
        // 1. Initialize mdOrder via checkout
        doCheckout("order-5") shouldBe true

        // 2. Stub processForm to return acsUrl
        coEvery { mockApi.processForm(any()) } returns ProcessFormResponse(
            errorCode = 0,
            acsUrl = "https://acs.example.com/challenge",
            paReq = "test-pareq",
            termUrl = "https://term.example.com",
            threeDSMethodURL = null
        )

        val latch = CountDownLatch(1)
        var capturedParam: WebChallengeParam? = null
        every { threeDS2WebFormDelegate.openWebChallenge(any()) } answers {
            capturedParam = firstArg()
            latch.countDown()
        }

        paymentManager.processNewCard("seToken", "order-5", "JOHN DOE", false, emptyFilledParams())

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedParam!!.acsUrl shouldBe "https://acs.example.com/challenge"
        capturedParam!!.paReq shouldBe "test-pareq"
        capturedParam!!.termUrl shouldBe "https://term.example.com"
        capturedParam!!.mdOrder shouldBe "order-5"
    }

    @Test
    fun processNewCardWithThreeDSMethodURLFinishesWithSDKNotConfigureException() {
        doCheckout("order-6") shouldBe true

        coEvery { mockApi.processForm(any()) } returns ProcessFormResponse(
            errorCode = 0,
            acsUrl = null, paReq = null, termUrl = null,
            threeDSMethodURL = "https://3ds.example.com/method"
        )
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns declinedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.processNewCard("seToken", "order-6", "JOHN DOE", false, emptyFilledParams())

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        (capturedResult!!.exception is SDKNotConfigureException) shouldBe true
    }

    @Test
    fun processNewCardWithoutThreeDSFinishesWithCheckOrderStatus() {
        doCheckout("order-7") shouldBe true

        coEvery { mockApi.processForm(any()) } returns ProcessFormResponse(
            errorCode = 0,
            acsUrl = null, paReq = null, termUrl = null, threeDSMethodURL = null
        )
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns depositedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.processNewCard("seToken", "order-7", "JOHN DOE", false, emptyFilledParams())

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe true
        capturedResult!!.exception shouldBe null
    }

    @Test
    fun processBindingCardWithAcsUrlOpensWebChallenge() {
        doCheckout("order-8") shouldBe true

        coEvery { mockApi.processBindingForm(any()) } returns ProcessFormResponse(
            errorCode = 0,
            acsUrl = "https://acs.bank.com/3ds",
            paReq = "pareq-binding",
            termUrl = "https://term.bank.com",
            threeDSMethodURL = null
        )

        val latch = CountDownLatch(1)
        var capturedParam: WebChallengeParam? = null
        every { threeDS2WebFormDelegate.openWebChallenge(any()) } answers {
            capturedParam = firstArg()
            latch.countDown()
        }

        paymentManager.processBindingCard("seToken", "order-8", emptyFilledParams())

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedParam!!.acsUrl shouldBe "https://acs.bank.com/3ds"
    }

    // ---- gPayProcessForm() routing ----

    @Test
    fun gPayProcessFormWithErrorTypFinishesWithError() {
        doCheckout("order-9") shouldBe true

        coEvery { mockApi.gPayProcessForm(any()) } returns ProcessFormGPayResponse(
            success = "false",
            data = net.payrdr.mobile.payment.sdk.api.entity.GPayData(
                orderId = "order-9",
                acsUrl = null, paReq = null, termUrl = null,
                errorTypeName = "PAYMENT_ERROR",
                threeDSAcsTransactionId = null, threeDSAcsRefNumber = null,
                threeDSAcsSignedContent = null, threeDSServerTransId = null
            )
        )
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns declinedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.gPayProcessForm(GooglePayProcessFormRequest("gpay-token", "order-9"))

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        (capturedResult!!.exception != null) shouldBe true
    }

    @Test
    fun gPayProcessFormWithAcsUrlOpensWebChallenge() {
        doCheckout("order-10") shouldBe true

        coEvery { mockApi.gPayProcessForm(any()) } returns ProcessFormGPayResponse(
            success = "true",
            data = net.payrdr.mobile.payment.sdk.api.entity.GPayData(
                orderId = "order-10",
                acsUrl = "https://gpay-acs.example.com",
                paReq = "gpay-pareq",
                termUrl = "https://gpay-term.example.com",
                errorTypeName = null,
                threeDSAcsTransactionId = null, threeDSAcsRefNumber = null,
                threeDSAcsSignedContent = null, threeDSServerTransId = null
            )
        )

        val latch = CountDownLatch(1)
        var capturedParam: WebChallengeParam? = null
        every { threeDS2WebFormDelegate.openWebChallenge(any()) } answers {
            capturedParam = firstArg()
            latch.countDown()
        }

        paymentManager.gPayProcessForm(GooglePayProcessFormRequest("gpay-token-2", "order-10"))

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedParam!!.acsUrl shouldBe "https://gpay-acs.example.com"
        capturedParam!!.paReq shouldBe "gpay-pareq"
        capturedParam!!.mdOrder shouldBe "order-10"
    }

    @Test
    fun gPayProcessFormWithoutErrorOrAcsFinishesWithCheckOrderStatus() {
        doCheckout("order-11") shouldBe true

        coEvery { mockApi.gPayProcessForm(any()) } returns ProcessFormGPayResponse(
            success = "true",
            data = null
        )
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns depositedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.gPayProcessForm(GooglePayProcessFormRequest("gpay-token-3", "order-11"))

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe true
        capturedResult!!.exception shouldBe null
    }

    // ---- processBindingCard() remaining branches ----

    @Test
    fun processBindingCardWithThreeDSMethodURLFinishesWithSDKNotConfigureException() {
        doCheckout("order-12") shouldBe true

        coEvery { mockApi.processBindingForm(any()) } returns ProcessFormResponse(
            errorCode = 0,
            acsUrl = null, paReq = null, termUrl = null,
            threeDSMethodURL = "https://3ds.example.com/method"
        )
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns declinedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.processBindingCard("seToken", "order-12", emptyFilledParams())

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        (capturedResult!!.exception is SDKNotConfigureException) shouldBe true
    }

    @Test
    fun processBindingCardWithoutThreeDSFinishesWithCheckOrderStatus() {
        doCheckout("order-13") shouldBe true

        coEvery { mockApi.processBindingForm(any()) } returns ProcessFormResponse(
            errorCode = 0,
            acsUrl = null, paReq = null, termUrl = null, threeDSMethodURL = null
        )
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns depositedPaymentInfo()

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.processBindingCard("seToken", "order-13", emptyFilledParams())

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe true
        capturedResult!!.exception shouldBe null
    }

    // ---- unbindCard() ----

    @Test
    fun unbindCardCallsPaymentApiUnbindCardAnonymous() {
        doCheckout("order-14") shouldBe true

        val latch = CountDownLatch(1)
        coEvery { mockApi.unbindCardAnonymous(any(), any()) } answers {
            latch.countDown()
            mockk(relaxed = true)
        }

        paymentManager.unbindCard("binding-42")

        latch.await(3, TimeUnit.SECONDS) shouldBe true
    }

    // ---- finishWithCheckOrderStatus() status mapping ----

    @Test
    fun finishWithCheckOrderStatusMapsApprovedToSuccess() {
        doCheckout("order-15") shouldBe true

        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns FinishedPaymentInfoResponse(
            status = "APPROVED",
            paymentDate = null, approvalCode = null, terminalId = null,
            refNum = null, panMasked = null, formattedAmount = null, actionCodeDescription = null
        )

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.finishWithCheckOrderStatus()

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe true
        capturedResult!!.exception shouldBe null
    }

    @Test
    fun finishWithCheckOrderStatusMapsCreatedToFailure() {
        doCheckout("order-16") shouldBe true

        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns FinishedPaymentInfoResponse(
            status = "CREATED",
            paymentDate = null, approvalCode = null, terminalId = null,
            refNum = null, panMasked = null, formattedAmount = null, actionCodeDescription = null
        )

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.finishWithCheckOrderStatus()

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe false
        capturedResult!!.exception shouldBe null
    }
}
