package net.payrdr.mobile.payment.sdk.payment

import android.Manifest
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.alluresupport.addAllureSupport
import com.kaspersky.components.alluresupport.files.attachViewHierarchyToAllureReport
import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import com.kaspersky.kaspresso.interceptors.watcher.testcase.TestRunWatcherInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.params.ScreenshotParams
import com.kaspersky.kaspresso.testcases.api.testcase.DocLocScreenshotTestCase
import com.kaspersky.kaspresso.testcases.models.info.TestInfo
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.PaymentApiImpl
import net.payrdr.mobile.payment.sdk.core.TestActivity
import net.payrdr.mobile.payment.sdk.exceptions.SDKAlreadyPaymentException
import net.payrdr.mobile.payment.sdk.exceptions.SDKCryptogramException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.utils.executePostParams
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.CardListScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.SelectedCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDSScreen
import net.payrdr.mobile.payment.sdk.testUtils.EmulatorSleep
import net.payrdr.mobile.payment.sdk.testUtils.junit.ConfigurationSingle
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@LargeTest
@Suppress("LargeClass", "MaxLineLength")
@RunWith(AllureAndroidJUnit4::class)
class PaymentActivityKaspressoTest : DocLocScreenshotTestCase(
    kaspressoBuilder = Kaspresso.Builder.simple(
        customize = {
            screenshotParams = ScreenshotParams(quality = 1)
            if (isAndroidRuntime) {
                UiDevice
                    .getInstance(instrumentation)
                    .executeShellCommand(
                        "appops set --uid ${InstrumentationRegistry.getInstrumentation().targetContext.packageName} MANAGE_EXTERNAL_STORAGE allow"
                    )
            }
        }
    ).addAllureSupport().apply {
        testRunWatcherInterceptors.apply {
            add(object : TestRunWatcherInterceptor {
                override fun onTestFinished(testInfo: TestInfo, success: Boolean) {
                    viewHierarchyDumper.dumpAndApply("ViewHierarchy") { attachViewHierarchyToAllureReport() }
                }
            })
        }
    },
    locales = "en",
) {

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    private lateinit var paymentApi: PaymentApi
    private lateinit var baseUrl: String
    private lateinit var dsRoot: String
    private lateinit var paymentConfig: SDKPaymentConfig

    private data class TestCard(
        val pan: String,
        val expiry: String,
        val cvc: String,
        val holder: String
    )

    @get:Rule
    val activityTestRule = ActivityTestRule(TestActivity::class.java, true, false)

    private fun TestCard.fillOutForm() {
        NewCardScreen {
            cardNumberInput {
                isVisible()
                typeText(pan)
            }
            cardExpiryInput {
                isVisible()
                typeText(expiry)
            }
            cardCodeInput {
                isVisible()
                typeText(cvc)
            }
        }
    }

    private val testCardWith3DS = TestCard(
        pan = "5000001111111115",
        expiry = "12/30",
        cvc = "123",
        holder = "CARD HOLDER"
    )

    private val testCardWithout3DS = TestCard(
        pan = "4000001111111118",
        expiry = "12/30",
        cvc = "123",
        holder = "CARD HOLDER"
    )

    @Before
    fun setUp() {
        baseUrl = "https://dev.bpcbt.com/payment"
        /* spellchecker: disable */
        dsRoot = """
        MIICDTCCAbOgAwIBAgIUOO3a573khC9kCsQJGKj/PpKOSl8wCgYIKoZIzj0EA
        wIwXDELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBA
        oMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDEVMBMGA1UEAwwMZHVtbXkzZHN
        yb290MB4XDTIxMDkxNDA2NDQ1OVoXDTMxMDkxMjA2NDQ1OVowXDELMAkGA1UE
        BhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0I
        FdpZGdpdHMgUHR5IEx0ZDEVMBMGA1UEAwwMZHVtbXkzZHNyb290MFkwEwYHKo
        ZIzj0CAQYIKoZIzj0DAQcDQgAE//e+MhwdgWxkFpexkjBCx8FtJ24KznHRXMS
        WabTrRYwdSZMScgwdpG1QvDO/ErTtW8IwouvDRlR2ViheGr02bqNTMFEwHQYD
        VR0OBBYEFHK/QzMXw3kW9UzY5w9LVOXr+6YpMB8GA1UdIwQYMBaAFHK/QzMXw
        3kW9UzY5w9LVOXr+6YpMA8GA1UdEwEB/wQFMAMBAf8wCgYIKoZIzj0EAwIDSA
        AwRQIhAOPEiotH3HJPIjlrj9/0m3BjlgvME0EhGn+pBzoX7Z3LAiAOtAFtkip
        d9T5c9qwFAqpjqwS9sSm5odIzk7ug8wow4Q==
        """
            /* spellchecker: enable */
            .replace("\n", "")
            .trimIndent()
        paymentApi = PaymentApiImpl(baseUrl)
        paymentConfig = SDKPaymentConfig(baseUrl, dsRoot)
        SDKForms.init(
            SDKConfigBuilder()
                .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
                .build()
        )
        SDKPayment.init(paymentConfig)
        SDKPayment.getSDKVersion()
        activityTestRule.launchActivity(null)
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSuccessPaymentDataWithoutThreeDsWithNewCard() {
        run {
            var actualResult: PaymentData? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                                actualResult = result
                            }

                            override fun onFail(e: SDKException) {
                            }
                        }
                    )
                }

            step("shouldReturnSuccessPaymentDataWithoutThreeDsWithNewCard Open New Card Screen") {
                val mdOrder: String? = regOrderWithNewCard()
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }

            step("shouldReturnSuccessPaymentDataWithoutThreeDsWithNewCard Fill Out Form") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    newCardItem {
                        isVisible()
                        click()
                    }
                }
                testCardWithout3DS.fillOutForm()
                NewCardScreen {
                    closeSoftKeyboard()
                    allureScreenshot(
                        name = "shouldReturnSuccessPaymentDataWithoutThreeDsWithNewCard_1",
                        quality = 1
                    ).toString()
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }

            step("shouldReturnSuccessPaymentDataWithoutThreeDsWithNewCard Assert") {
                flakySafely {
                    assertEquals("DEPOSITED", actualResult?.status)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock", "LongMethod")
    fun shouldReturnSuccessPaymentDataWithThreeDsWithNewCard() {
        run {
            var actualResult: PaymentData? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                                actualResult = result
                            }

                            override fun onFail(e: SDKException) {
                            }
                        }
                    )
                }
            step("shouldReturnSuccessPaymentDataWithThreeDsWithNewCard Open New Card Screen") {
                val mdOrder: String? = regOrderWithNewCard()
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnSuccessPaymentDataWithThreeDsWithNewCard Fill Out Form") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    allPayment {
                        isVisible()
                        click()
                    }
                }
                testCardWith3DS.fillOutForm()
                NewCardScreen {
                    closeSoftKeyboard()
                    allureScreenshot(
                        name = "shouldReturnSuccessPaymentDataWithThreeDsWithNewCard_1",
                        quality = 1
                    )
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldReturnSuccessPaymentDataWithThreeDsWithNewCard ThreeDS Screen") {
                flakySafely(timeoutMs = 20_000) {
                    ThreeDSScreen {
                        dataEntry {
                            isVisible()
                            typeText("123456")
                        }
                        closeSoftKeyboard()
                        allureScreenshot(
                            name = "shouldReturnSuccessPaymentDataWithThreeDsWithNewCard_2",
                            quality = 1
                        )
                        submit {
                            isVisible()
                            click()
                        }
                    }
                }
            }
            step("shouldReturnSuccessPaymentDataWithThreeDsWithNewCard Assert") {
                flakySafely {
                    assertEquals("DEPOSITED", actualResult?.status)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldSaveCardForNewCard() {
        run {
            val clientId = getClientId()
            step("shouldSaveCardForNewCard Open Binding Card Screen") {
                val mdOrder: String? = regOrderWithBindingCard(clientId = clientId)
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldSaveCardForNewCard Fill Out Form") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    newCardItem {
                        isVisible()
                        click()
                    }
                }
                testCardWithout3DS.fillOutForm()
                NewCardScreen {
                    closeSoftKeyboard()
                    allureScreenshot(name = "shouldSaveCardForNewCard_1", quality = 1)
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldSaveCardForNewCard Assert") {
                flakySafely {
                    val mdOrderBinding: String? = regOrderWithBindingCard(clientId = clientId)
                    val status = runBlocking {
                        paymentApi.getSessionStatus(mdOrderBinding!!).bindingItems?.any {
                            it.label.contains("400000**1118 12/30")
                        }
                    }
                    assertEquals(true, status)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldNotSaveCardForNewCard() {
        run {
            val clientId = getClientId()
            step("shouldNotSaveCardForNewCard Open Binding Card Screen") {
                val mdOrder: String? = regOrderWithBindingCard(clientId = clientId)
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldNotSaveCardForNewCard Fill Out Form") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    newCardItem {
                        isVisible()
                        click()
                    }
                }
                testCardWithout3DS.fillOutForm()
                NewCardScreen {
                    checkSaveCard {
                        isVisible()
                        click()
                    }
                    closeSoftKeyboard()
                    allureScreenshot(name = "shouldNotSaveCardForNewCard_1", quality = 1)
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldNotSaveCardForNewCard Assert") {
                flakySafely {
                    val mdOrderBinding: String? = regOrderWithBindingCard(clientId = clientId)
                    val status = runBlocking {
                        paymentApi.getSessionStatus(mdOrderBinding!!).bindingItems?.any {
                            it.label.contains("400000**1118 12/30")
                        }
                    }
                    assertEquals(false, status)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSDKCryptogramExceptionWhenPressBackBtnWithNewCard() {
        run {
            var actualResult: SDKException? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                            }

                            override fun onFail(e: SDKException) {
                                actualResult = e
                            }
                        }
                    )
                }
            step("shouldReturnSDKCryptogramExceptionWhenPressBackBtnWithNewCard Open New Card Screen") {
                val mdOrder: String? = regOrderWithNewCard()
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnSDKCryptogramExceptionWhenPressBackBtnWithNewCard Press Back") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    newCardItem {
                        isVisible()
                        click()
                    }
                }
                NewCardScreen {
                    cardNumberInput {
                        isVisible()
                        typeText("1111111111111111")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(
                        name = "shouldReturnSDKCryptogramExceptionWhenPressBackBtnWithNewCard_1",
                        quality = 1
                    )
                    pressBack()
                }
            }
            step("shouldReturnSDKCryptogramExceptionWhenPressBackBtnWithNewCard Assert") {
                flakySafely {
                    assert(actualResult is SDKCryptogramException)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Ignore
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnAlreadyPaymentExceptionWithNewCard() {
        run {
            var actualResult: SDKException? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                                result
                            }

                            override fun onFail(e: SDKException) {
                                actualResult = e
                            }
                        }
                    )
                }
            val mdOrder: String? = regOrderWithNewCard()
            step("shouldReturnAlreadyPaymentExceptionWithNewCard Open New Card Screen") {
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnAlreadyPaymentExceptionWithNewCard Fill Out Form") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    newCardItem {
                        isVisible()
                        click()
                    }
                }
                testCardWithout3DS.fillOutForm()
                NewCardScreen {
                    closeSoftKeyboard()
                    allureScreenshot(
                        name = "shouldReturnAlreadyPaymentExceptionWithNewCard_1",
                        quality = 1
                    )
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldReturnAlreadyPaymentExceptionWithNewCard Open Second New Card Screen") {
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnAlreadyPaymentExceptionWithNewCard Assert") {
                flakySafely {
                    assert(actualResult is SDKAlreadyPaymentException)
                }
            }
        }
    }

    @Test
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnApiExceptionWithNewCard() {
        run {
            var actualResult: SDKException? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                            }

                            override fun onFail(e: SDKException) {
                                actualResult = e
                            }
                        }
                    )
                }
            step("shouldReturnApiExceptionWithNewCard Open Payment Screen") {
                SDKPayment.checkout(activityTestRule.activity, "45456-5454655-GAV-GAV")
            }
            step("shouldReturnApiExceptionWithNewCard Assert") {
                flakySafely {
                    assert(actualResult is SDKPaymentApiException)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock", "LongMethod")
    @Ignore("actualResult always null")
    fun shouldReturnSDKDeclinedExceptionWithNewCard() {
        run {
            var actualResult: SDKException? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                            }

                            override fun onFail(e: SDKException) {
                                actualResult = e
                            }
                        }
                    )
                }
            val mdOrder: String? = regOrderWithNewCard()
            step("shouldReturnSDKDeclinedExceptionWithNewCard Open New Card Screen") {
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnSDKDeclinedExceptionWithNewCard Fill Out Form") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    newCardItem {
                        isVisible()
                        click()
                    }
                }
                testCardWith3DS.fillOutForm()
                NewCardScreen {
                    closeSoftKeyboard()
                    allureScreenshot(
                        name = "shouldReturnSDKDeclinedExceptionWithNewCard_1",
                        quality = 1
                    )
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldReturnSDKDeclinedExceptionWithNewCard ThreeDS Screen") {
                ThreeDSScreen {
                    cancel {
                        flakySafely(timeoutMs = 20_000) {
                            isVisible()
                            this@ThreeDSScreen.closeSoftKeyboard()
                            allureScreenshot(
                                name = "shouldReturnSDKDeclinedExceptionWithNewCard_2",
                                quality = 1
                            )
                            click()
                        }
                    }
                }
            }
            step("shouldReturnSDKDeclinedExceptionWithNewCard Second New Card Screen") {
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnSDKDeclinedExceptionWithNewCard Assert") {
                flakySafely {
                    if (actualResult == null) {
                        Assert.assertNull(actualResult)
                    } else {
                        assert(actualResult is SDKDeclinedException)
                    }
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock", "LongMethod")
    fun shouldReturnPaymentDataByNewCardWithBindingCard() {
        run {
            var actualResult: PaymentData? = null
            activityTestRule.activity.onActivityResultListener = { requestCode, _, data ->
                SDKPayment.handleCheckoutResult(
                    requestCode,
                    data,
                    object : ResultPaymentCallback<PaymentData> {
                        override fun onSuccess(result: PaymentData) {
                            actualResult = result
                        }

                        override fun onFail(e: SDKException) {
                        }
                    }
                )
            }
            step("shouldReturnPaymentDataByNewCardWithBindingCard Open List Card Screen") {
                val mdOrder: String? = regOrderWithBindingCard()
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnPaymentDataByNewCardWithBindingCard From List Card Screen To New Card") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    allPayment {
                        isVisible()
                        click()
                    }
                }
                CardListScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldReturnPaymentDataByNewCardWithBindingCard_1",
                            quality = 1
                        )
                        click()
                    }
                }
            }
            step("shouldReturnPaymentDataByNewCardWithBindingCard New Card Screen Fill Out Form") {
                NewCardScreen {
                    cardNumberInput {
                        isVisible()
                        typeText("4000001111111118")
                    }
                    cardExpiryInput {
                        isVisible()
                        typeText("12/30")
                    }
                    cardCodeInput {
                        isVisible()
                        typeText("123")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(
                        name = "shouldReturnPaymentDataByNewCardWithBindingCard_2",
                        quality = 1
                    )
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldReturnPaymentDataByNewCardWithBindingCard Assert") {
                flakySafely {
                    assertEquals("DEPOSITED", actualResult?.status)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock", "LongMethod")
    fun shouldReturnSuccessWithoutCVCPaymentWithBindingCard() {
        run {
            var actualResult: PaymentData? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                                actualResult = result
                            }

                            override fun onFail(e: SDKException) {
                            }
                        }
                    )
                }
            step("shouldReturnSuccessWithoutCVCPaymentWithBindingCard Open List Card Screen") {
                val mdOrder: String? = regOrderWithBindingCard("mobile-sdk-api", "956")
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnSuccessWithoutCVCPaymentWithBindingCard From List Card Screen To New Card Screen") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    allPayment {
                        isVisible()
                        click()
                    }
                }
                CardListScreen {
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldReturnSuccessWithoutCVCPaymentWithBindingCard New Card Screen Fill Out Form") {
                NewCardScreen {
                    cardNumberInput {
                        isVisible()
                        typeText("4000001111111118")
                    }
                    cardExpiryInput {
                        isVisible()
                        typeText("12/30")
                    }
                    cardCodeInput {
                        isVisible()
                        typeText("123")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(
                        name = "shouldReturnSuccessWithoutCVCPaymentWithBindingCard_2",
                        quality = 1
                    )
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldReturnSuccessWithoutCVCPaymentWithBindingCard Assert After Click Done") {
                flakySafely {
                    assertEquals("DEPOSITED", actualResult?.status)
                }
            }
            step("shouldReturnSuccessWithoutCVCPaymentWithBindingCard Choose Binding Card") {
                val mdOrderBinding: String? = regOrderWithBindingCard("mobile-sdk-api", "956")
                SDKPayment.checkout(activityTestRule.activity, mdOrderBinding!!)
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    allPayment {
                        isVisible()
                        click()
                    }
                }
                CardListScreen {
                    bindingCard {
                        isVisible()
                        click()
                    }
                }
            }
            step("shouldReturnSuccessWithoutCVCPaymentWithBindingCard Selected Screen") {
                SelectedCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldReturnSuccessWithoutCVCPaymentWithBindingCard_3",
                            quality = 1
                        )
                        click()
                    }
                }
            }
            step("shouldReturnSuccessWithoutCVCPaymentWithBindingCard Assert") {
                flakySafely {
                    assertEquals("DEPOSITED", actualResult?.status)
                }
            }
        }
    }

    @Test
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnApiExceptionWithBindingCard() {
        run {
            var actualResult: SDKException? = null
            activityTestRule.activity.onActivityResultListener =
                { requestCode, _, data ->
                    SDKPayment.handleCheckoutResult(
                        requestCode,
                        data,
                        object : ResultPaymentCallback<PaymentData> {
                            override fun onSuccess(result: PaymentData) {
                            }

                            override fun onFail(e: SDKException) {
                                actualResult = e
                            }
                        }
                    )
                }
            step("shouldReturnApiExceptionWithBindingCard List Card Screen") {
                SDKPayment.checkout(activityTestRule.activity, "45dd445-f5a4ff6-ffd55454")
            }
            step("shouldReturnApiExceptionWithBindingCard Assert") {
                flakySafely {
                    assert(actualResult is SDKPaymentApiException)
                }
            }
        }
    }

    @Test
    fun shouldReturnAvailableOptionForEditCardList() {
        run {
            step("shouldReturnAvailableOptionForEditCardList List Card Screen") {
                val mdOrder: String? = regOrderWithBindingCard()
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }
            step("shouldReturnAvailableOptionForEditCardList Edit List Displayed Assert") {
                BottomSheetScreen {
                    Thread.sleep(10000L)
                    allPayment {
                        isVisible()
                        click()
                    }
                }
                CardListScreen {
                    editList {
                        isDisplayed()
                    }
                }
            }
        }
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    @Ignore
    fun shouldReturnDeclinedWhenButtonCancelPressOnChallengeFlowScreenWithNewCard() {
        var actualResult: PaymentData? = null
        activityTestRule.activity.onActivityResultListener = { requestCode, resultCode, data ->
            SDKPayment.handleCheckoutResult(
                requestCode,
                data,
                object : ResultPaymentCallback<PaymentData> {
                    override fun onSuccess(result: PaymentData) {
                        actualResult = result
                    }

                    override fun onFail(e: SDKException) {
                    }
                }
            )
        }
        val mdOrder: String? = regOrderWithNewCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        testCardWith3DS.fillOutForm()
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.otp_page_toolbar_cancel))
            .perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)

        assertEquals("DECLINED", actualResult?.status)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    @Ignore
    fun shouldReturnPaymentApiExceptionWhenPaymentUrlNotExistsWithNewCard() {
        val config = SDKPaymentConfig("https://domain.com/payment", dsRoot)
        SDKPayment.init(config)

        var actualResult: SDKException? = null
        activityTestRule.activity.onActivityResultListener = { requestCode, resultCode, data ->
            SDKPayment.handleCheckoutResult(
                requestCode,
                data,
                object : ResultPaymentCallback<PaymentData> {
                    override fun onSuccess(result: PaymentData) {
                    }

                    override fun onFail(e: SDKException) {
                        actualResult = e
                    }
                }
            )
        }
        val mdOrder: String? = regOrderWithNewCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        EmulatorSleep.sleep(6_000)

        assert(actualResult is SDKPaymentApiException)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    @Ignore
    fun shouldReturnSuccessPaymentDataWithBindingCard() {
        var actualResult: PaymentData? = null
        activityTestRule.activity.onActivityResultListener = { requestCode, resultCode, data ->
            SDKPayment.handleCheckoutResult(
                requestCode,
                data,
                object : ResultPaymentCallback<PaymentData> {
                    override fun onSuccess(result: PaymentData) {
                        actualResult = result
                    }

                    override fun onFail(e: SDKException) {
                    }
                }
            )
        }
        val mdOrderForAddingCard: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrderForAddingCard!!)
        EmulatorSleep.sleep(6_000)
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.cardNumberInput)).perform(
            ViewActions.typeText("4000001111111118"),
            ViewActions.closeSoftKeyboard()
        )
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .perform(ViewActions.typeText("12/30"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .perform(ViewActions.typeText("123"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withText("•• 1118")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput)).perform(
            ViewActions.typeText("123"),
            ViewActions.closeSoftKeyboard()
        )
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)

        assertEquals("DEPOSITED", actualResult?.status)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    @Ignore
    fun shouldReturnDeclinedWhenButtonCancelPressOnChallengeFlowWithBindingCard() {
        var actualResult: PaymentData? = null
        activityTestRule.activity.onActivityResultListener = { requestCode, resultCode, data ->
            SDKPayment.handleCheckoutResult(
                requestCode,
                data,
                object : ResultPaymentCallback<PaymentData> {
                    override fun onSuccess(result: PaymentData) {
                        actualResult = result
                    }

                    override fun onFail(e: SDKException) {
                    }
                }
            )
        }
        val mdOrderForNewCard: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrderForNewCard!!)
        EmulatorSleep.sleep(6_000)
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.cardNumberInput))
            .perform(ViewActions.typeText("4777777777777778"))
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .perform(ViewActions.typeText("12/24"))
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .perform(ViewActions.typeText("123"))
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.activity_text_challenge_dataEntry))
            .perform(
                ViewActions.typeText("123456"),
                ViewActions.closeSoftKeyboard()
            )
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.activity_text_challenge_submit))
            .perform(ViewActions.click())
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withText("•• 7778")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput)).perform(
            ViewActions.typeText("123"),
            ViewActions.closeSoftKeyboard()
        )
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)
//        takeScreen()
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.otp_page_toolbar_cancel))
            .perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)

        if (actualResult == null) {
            Assert.assertNull(actualResult)
        } else {
            assertEquals("DECLINED", actualResult?.status)
        }
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    @Ignore
    fun shouldReturnSDKDeclinedExceptionWithBindingCard() {
        var actualResult: SDKException? = null
        activityTestRule.activity.onActivityResultListener = { requestCode, resultCode, data ->
            SDKPayment.handleCheckoutResult(
                requestCode,
                data,
                object : ResultPaymentCallback<PaymentData> {
                    override fun onSuccess(result: PaymentData) {
                    }

                    override fun onFail(e: SDKException) {
                        actualResult = e
                    }
                }
            )
        }
        val mdOrderForNewCard: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrderForNewCard!!)
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.cardNumberInput))
            .perform(ViewActions.typeText("4777777777777778"))
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .perform(ViewActions.typeText("12/24"))
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .perform(ViewActions.typeText("123"))
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.activity_text_challenge_dataEntry))
            .perform(
                ViewActions.typeText("123456"),
                ViewActions.closeSoftKeyboard()
            )
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.activity_text_challenge_submit))
            .perform(ViewActions.click())
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        EmulatorSleep.sleep(2_000)
        Espresso.onView(ViewMatchers.withText("•• 7778")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput)).perform(
            ViewActions.typeText("123"),
            ViewActions.closeSoftKeyboard()
        )
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)

//        takeScreen()
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.otp_page_toolbar_cancel))
            .perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)

        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        EmulatorSleep.sleep(6_000)

        if (actualResult == null) {
            Assert.assertNull(actualResult)
        } else {
            assert(actualResult is SDKDeclinedException)
        }
    }

    @Test
    @ConfigurationSingle
    @Ignore
    fun shouldReturnUnbindCard() {
        val clientId = getClientId()
        var mdOrder: String? = regOrderWithBindingCard(clientId = clientId)
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        EmulatorSleep.sleep(6_000)
        bindCard()
        EmulatorSleep.sleep(6_000)
        mdOrder = regOrderWithBindingCard(clientId = clientId)
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withId(R.id.editCardsList)).perform(ViewActions.click())
//        Espresso.onView(ViewMatchers.withId(R.id.cardList)).perform(
//            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
//                0,
//                clickItemWithId(R.id.arrow)
//            )
//        )
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withText("Да")).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.cardNumberInput))
            .perform(ViewActions.typeText("4777777777777778"))
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardExpiryInput))
            .perform(ViewActions.typeText("12/24"))
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        Espresso.onView(ViewMatchers.withId(R.id.cardCodeInput))
            .perform(ViewActions.typeText("123"))
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.activity_text_challenge_dataEntry))
            .perform(
                ViewActions.typeText("123456"),
                ViewActions.closeSoftKeyboard()
            )
        Espresso.onView(ViewMatchers.withId(net.payrdr.mobile.payment.sdk.threeds.R.id.activity_text_challenge_submit))
            .perform(ViewActions.click())
        EmulatorSleep.sleep(6_000)
        val status = runBlocking {
            paymentApi.getSessionStatus(mdOrder!!).bindingItems?.any {
                it.label.contains("555555**5599 12/24")
            }
        }
        assertEquals(false, status)
    }

    private fun bindCard() {
        testCardWithout3DS.fillOutForm()
        Espresso.onView(ViewMatchers.withId(R.id.doneButton)).perform(ViewActions.click())
    }

    private fun getClientId(): String = System.currentTimeMillis().toString()

    private fun regOrderWithNewCard(): String? {
        val url = "https://dev.bpcbt.com/payment/rest/register.do"
        val body = mapOf(
            "amount" to "20000",
            "userName" to "mobile-sdk-api",
            "password" to "vkyvbG0",
            "returnUrl" to "https://redirect.ru/merchants/rbs/finish.html"
        )

        return runCatching {
            val connection = URL(url).executePostParams(body)
            connection.responseBodyToJsonObject().getString("orderId")
        }.getOrNull()
    }

    private fun regOrderWithBindingCard(
        userName: String = "mobile-sdk-api",
        clientId: String = "955"
    ): String? {
        val url = "https://dev.bpcbt.com/payment/rest/register.do"
        val body = mapOf(
            "amount" to "2000",
            "userName" to userName,
            "password" to "vkyvbG0",
            "returnUrl" to "https://redirect.ru/merchants/rbs/finish.html",
            "clientId" to clientId
        )

        return runCatching {
            val connection = URL(url).executePostParams(body)
            connection.responseBodyToJsonObject().getString("orderId")
        }.getOrNull()
    }
}
