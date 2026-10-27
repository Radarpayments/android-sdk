package net.payrdr.mobile.payment.sdk.payment

import androidx.test.filters.MediumTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.payrdr.mobile.payment.sdk.DeleteCardHandler
import net.payrdr.mobile.payment.sdk.SDKCanceledByUserException
import net.payrdr.mobile.payment.sdk.SDKForms
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
import net.payrdr.mobile.payment.sdk.exceptions.SDKCryptogramException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKNotConfigureException
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.exceptions.SDKTransactionException
import net.payrdr.mobile.payment.sdk.model.FilledAdditionalPayerParams
import net.payrdr.mobile.payment.sdk.payment.model.GooglePayProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.PaymentApiVersion
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.utils.SessionIdConverter
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
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
        (capturedResult!!.exception is SDKTransactionException) shouldBe true
    }

    @Test
    fun finishWithCheckOrderStatusMapsDeclinedToDeclinedExceptionWithGatewayReason() {
        doCheckout("order-20") shouldBe true

        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns declinedPaymentInfo().copy(
            actionCode = "116",
            displayErrorMessage = "Operation declined, please check your details and try again.",
            errorTypeName = "INSUFFICIENT_FUNDS"
        )

        val result = finishWithCheckOrderStatusResult()

        result.isSuccess shouldBe false
        val exception = result.exception as SDKDeclinedException
        exception.message shouldBe "Operation declined, please check your details and try again."
        exception.actionCode shouldBe "116"
        exception.errorType shouldBe "INSUFFICIENT_FUNDS"
    }

    @Test
    fun finishWithCheckOrderStatusMapsCreatedWithActionCodeToDeclinedException() {
        doCheckout("order-21") shouldBe true

        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns FinishedPaymentInfoResponse(
            status = "CREATED",
            paymentDate = null, approvalCode = null, terminalId = null,
            refNum = null, panMasked = null, formattedAmount = null,
            actionCodeDescription = "Some payment data is invalid",
            actionCode = "71015",
            errorTypeName = "DATA_INPUT_ERROR"
        )

        val result = finishWithCheckOrderStatusResult()

        result.isSuccess shouldBe false
        val exception = result.exception as SDKDeclinedException
        exception.message shouldBe "Some payment data is invalid"
        exception.actionCode shouldBe "71015"
        exception.errorType shouldBe "DATA_INPUT_ERROR"
    }

    @Test
    fun processNewCardWithErrorCodeFinishesWithPaymentApiException() {
        doCheckout("order-22") shouldBe true

        coEvery { mockApi.processForm(any()) } returns ProcessFormResponse(
            errorCode = 5,
            acsUrl = null, paReq = null, termUrl = null, threeDSMethodURL = null,
            error = "Invalid payer data: Mobile phone (Invalid value)"
        )
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

        paymentManager.processNewCard("seToken", "order-22", "JOHN DOE", false, emptyFilledParams())

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe false
        (capturedResult!!.exception is SDKPaymentApiException) shouldBe true
        capturedResult!!.exception!!.message shouldBe "Invalid payer data: Mobile phone (Invalid value)"
        verify(exactly = 0) { threeDS2WebFormDelegate.openWebChallenge(any()) }
    }

    @Test
    fun checkoutExpiredSessionWithUnfinishedOrderFinishesWithTransactionException() {
        coEvery { mockApi.getSessionStatus(any()) } returns minimalSessionStatus(remainingSecs = null)
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

        paymentManager.checkout("order-23", gPayClicked = false, versionApi = PaymentApiVersion.V1)

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe false
        (capturedResult!!.exception is SDKTransactionException) shouldBe true
    }

    private fun finishWithCheckOrderStatusResult(): PaymentResult {
        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.finishWithCheckOrderStatus()

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        return capturedResult!!
    }

    // ---- restore() / onDestroy() ----

    @Test
    fun restoreSetsOrderWithoutCheckout() {
        coEvery { mockApi.getFinishedPaymentInfo(any()) } returns depositedPaymentInfo()
        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.restore("order-20", PaymentApiVersion.V1)
        paymentManager.finishWithCheckOrderStatus()

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.sessionId shouldBe "order-20"
        verify(exactly = 0) { cardFormDelegate.openBottomSheet(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun restoreConvertsOrderToSessionIdForApiV2() {
        val order = "8e5b9c6d-1f2a-4b3c-9d8e-7f6a5b4c3d2e"
        paymentManager.restore(order, PaymentApiVersion.V2)

        val sessionIdField = PaymentManagerImpl::class.java.getDeclaredField("sessionId")
            .apply { isAccessible = true }
        sessionIdField.get(paymentManager) shouldBe SessionIdConverter.mdOrderToSessionId(order)
    }

    @Test
    fun onDestroyReleasesOwnDeleteCardHandler() {
        SDKForms.deleteCardHandlerOrNull shouldNotBe null

        paymentManager.onDestroy()

        SDKForms.deleteCardHandlerOrNull shouldBe null
    }

    @Test
    fun onDestroyKeepsDeleteCardHandlerSetByAnotherOwner() {
        val hostHandler = mockk<DeleteCardHandler>(relaxed = true)
        SDKForms.deleteCardHandler = hostHandler

        paymentManager.onDestroy()

        SDKForms.deleteCardHandlerOrNull shouldBe hostHandler
    }

    // ---- finishWithError() ----

    @Test
    fun finishWithErrorReturnsCancellationImmediatelyWithoutServerCalls() {
        doCheckout("order-17") shouldBe true
        coEvery { mockApi.getSessionStatus(any()) } throws SocketTimeoutException("timeout")
        coEvery { mockApi.getFinishedPaymentInfo(any()) } throws SocketTimeoutException("timeout")
        val cancellation = SDKCanceledByUserException()
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers { capturedResult = firstArg() }

        paymentManager.finishWithError(cancellation)

        // Delivered synchronously: no coroutine and no network round trip.
        capturedResult!!.isSuccess shouldBe false
        capturedResult!!.exception shouldBe cancellation
        capturedResult!!.sessionId shouldBe "order-17"
        coVerify(exactly = 0) { mockApi.getFinishedPaymentInfo(any()) }
    }

    @Test
    fun finishWithErrorKeepsOriginalErrorWhenStatusCheckFails() {
        doCheckout("order-18") shouldBe true
        val checkError = SocketTimeoutException("timeout")
        coEvery { mockApi.getSessionStatus(any()) } throws checkError
        val original = SDKCryptogramException(cause = null)

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.finishWithError(original)

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.isSuccess shouldBe false
        capturedResult!!.exception shouldBe original
        original.suppressed.toList() shouldBe listOf(checkError)
    }

    @Test
    fun finishWithErrorWrapsNonSdkErrorKeepingItsMessage() {
        doCheckout("order-19") shouldBe true
        coEvery { mockApi.getSessionStatus(any()) } throws SocketTimeoutException("timeout")
        val original = IllegalStateException("keys are not available")

        val latch = CountDownLatch(1)
        var capturedResult: PaymentResult? = null
        every { activityDelegate.finishActivityWithResult(any()) } answers {
            capturedResult = firstArg()
            latch.countDown()
        }

        paymentManager.finishWithError(original)

        latch.await(3, TimeUnit.SECONDS) shouldBe true
        capturedResult!!.exception!!.message shouldBe "keys are not available"
        capturedResult!!.exception!!.cause shouldBe original
    }
}
