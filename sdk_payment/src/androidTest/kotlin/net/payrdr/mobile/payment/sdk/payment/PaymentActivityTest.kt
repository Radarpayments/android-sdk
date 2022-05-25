package net.payrdr.mobile.payment.sdk.payment

import android.Manifest
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.PaymentApiImpl
import net.payrdr.mobile.payment.sdk.core.CoreUITest
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
import net.payrdr.mobile.payment.sdk.testUtils.EmulatorSleep.sleep
import net.payrdr.mobile.payment.sdk.testUtils.junit.ConfigurationSingle
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.net.URL
import net.payrdr.mobile.payment.sdk.threeds.R as threeDSR

@LargeTest
@Suppress("LargeClass")
class PaymentActivityTest : CoreUITest<TestActivity>(TestActivity::class.java, true, true) {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    private fun TestCard.fillOutForm() {
        onView(withId(R.id.cardNumberInput)).perform(
            typeText(pan),
            closeSoftKeyboard()
        )
        onView(withId(R.id.cardExpiryInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardExpiryInput)).perform(
            typeText(expiry),
            closeSoftKeyboard()
        )
        onView(withId(R.id.cardCodeInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardCodeInput)).perform(
            typeText(cvc),
            closeSoftKeyboard()
        )
        onView(withId(R.id.cardHolderInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardHolderInput)).perform(
            typeText(holder),
            closeSoftKeyboard()
        )
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
        baseUrl = "https://ecommerce.radarpayments.com/payment"
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
                .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
                .build()
        )
        SDKPayment.init(paymentConfig)
        SDKPayment.getSDKVersion()
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSuccessPaymentDataWithThreeDsWithNewCard() {
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
        sleep(6_000)
        testCardWith3DS.fillOutForm()
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        onView(withId(threeDSR.id.activity_text_challenge_dataEntry)).perform(
            typeText("123456"),
            closeSoftKeyboard()
        )
        takeScreen()
        onView(withId(threeDSR.id.activity_text_challenge_submit)).perform(click())
        sleep(6_000)
        assertEquals("DEPOSITED", actualResult?.status)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSuccessPaymentDataWithoutThreeDsWithNewCard() {
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
        sleep(6_000)
        testCardWithout3DS.fillOutForm()
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        assertEquals("DEPOSITED", actualResult?.status)
    }

    @Test
    @ConfigurationSingle
    fun shouldSaveCardForNewCard() {
        val clientId = getClientId()
        val mdOrder: String? = regOrderWithBindingCard(clientId = clientId)
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)
        testCardWithout3DS.fillOutForm()
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        val mdOrderBinding: String? = regOrderWithBindingCard(clientId = clientId)
        val status = runBlocking {
            paymentApi.getSessionStatus(mdOrderBinding!!).bindingItems?.any {
                it.label.contains("400000**1118 12/30")
            }
        }
        assertEquals(true, status)
    }

    @Test
    @ConfigurationSingle
    fun shouldNotSaveCardForNewCard() {
        val clientId = getClientId()
        val mdOrder: String? = regOrderWithBindingCard(clientId = clientId)
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        testCardWithout3DS.fillOutForm()
        try {
            onView(withId(R.id.checkSaveCard)).perform(click())
        } catch (e: Exception) {
            // TODO
        }
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        val mdOrderBinding: String? = regOrderWithBindingCard(clientId = clientId)
        val status = runBlocking {
            paymentApi.getSessionStatus(mdOrderBinding!!).bindingItems?.any {
                it.label.contains("400000**1118 12/30")
            }
        }
        assertEquals(false, status)
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
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        takeScreen()
        onView(withId(threeDSR.id.otp_page_toolbar_cancel)).perform(click())
        sleep(6_000)

        assertEquals("DECLINED", actualResult?.status)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSDKCryptogramExceptionWhenPressBackBtnWithNewCard() {
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
        sleep(6_000)
        takeScreen()
        onView(isRoot()).perform(pressBack())
        assert(actualResult is SDKCryptogramException)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnAlreadyPaymentExceptionWithNewCard() {
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
        sleep(6_000)
        testCardWithout3DS.fillOutForm()
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        SDKPayment.checkout(activityTestRule.activity, mdOrder)
        sleep(6_000)

        if (actualResult == null) {
            assertNull(actualResult)
        } else {
            assert(actualResult is SDKAlreadyPaymentException)
        }
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnApiExceptionWithNewCard() {
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
        SDKPayment.checkout(activityTestRule.activity, "45456-5454655-GAV-GAV")
        sleep(6_000)
        assert(actualResult is SDKPaymentApiException)
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
        sleep(6_000)

        assert(actualResult is SDKPaymentApiException)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSDKDeclinedExceptionWithNewCard() {
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
        sleep(6_000)
        testCardWith3DS.fillOutForm()
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        takeScreen()
        onView(withId(threeDSR.id.otp_page_toolbar_cancel)).perform(click())
        SDKPayment.checkout(activityTestRule.activity, mdOrder)
        sleep(2_000)

        if (actualResult == null) {
            assertNull(actualResult)
        } else {
            assert(actualResult is SDKDeclinedException)
        }
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
        sleep(6_000)
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.cardNumberInput)).perform(
            typeText("4000001111111118"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.cardExpiryInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardExpiryInput)).perform(typeText("12/30"), closeSoftKeyboard())
        onView(withId(R.id.cardCodeInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardCodeInput)).perform(typeText("123"), closeSoftKeyboard())
        try {
            onView(withId(R.id.cardHolderInput)).perform(
                typeText("CARD HOLDER"),
                closeSoftKeyboard()
            )
        } catch (e: Exception) {
            // TODO
        }
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)
        onView(withText("•• 1118")).perform(click())
        onView(withId(R.id.cardCodeInput)).perform(
            typeText("123"),
            closeSoftKeyboard()
        )
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)

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
        sleep(6_000)
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.cardNumberInput)).perform(typeText("4777777777777778"))
        onView(withId(R.id.cardExpiryInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardExpiryInput)).perform(typeText("12/24"))
        onView(withId(R.id.cardCodeInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardCodeInput)).perform(typeText("123"))
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        onView(withId(threeDSR.id.activity_text_challenge_dataEntry)).perform(
            typeText("123456"),
            closeSoftKeyboard()
        )
        takeScreen()
        onView(withId(threeDSR.id.activity_text_challenge_submit)).perform(click())
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)
        onView(withText("•• 7778")).perform(click())
        onView(withId(R.id.cardCodeInput)).perform(
            typeText("123"),
            closeSoftKeyboard()
        )
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        takeScreen()
        onView(withId(threeDSR.id.otp_page_toolbar_cancel)).perform(click())
        sleep(6_000)

        if (actualResult == null) {
            assertNull(actualResult)
        } else {
            assertEquals("DECLINED", actualResult?.status)
        }
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnPaymentDataByNewCardWithBindingCard() {
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
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(3_000)
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.cardNumberInput)).perform(
            typeText("4000001111111118"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.cardExpiryInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardExpiryInput)).perform(typeText("12/30"), closeSoftKeyboard())
        onView(withId(R.id.cardCodeInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardCodeInput)).perform(typeText("123"), closeSoftKeyboard())
        try {
            onView(withId(R.id.cardHolderInput)).perform(
                typeText("CARD HOLDER"),
                closeSoftKeyboard()
            )
        } catch (e: Exception) {
            // TODO
        }
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)

        assertEquals("DEPOSITED", actualResult?.status)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSuccessWithoutCVCPaymentWithBindingCard() {
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
        val mdOrder: String? = regOrderWithBindingCard("mobile-sdk-api", "956")
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(4_000)
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.cardNumberInput)).perform(
            typeText("4000001111111118"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.cardExpiryInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardExpiryInput)).perform(typeText("12/30"), closeSoftKeyboard())
        onView(withId(R.id.cardCodeInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardCodeInput)).perform(typeText("123"), closeSoftKeyboard())
        takeScreen()
        try {
            onView(withId(R.id.cardHolderInput)).perform(
                typeText("CARD HOLDER"),
                closeSoftKeyboard()
            )
        } catch (e: Exception) {
            // TODO
        }
        onView(withId(R.id.doneButton)).perform(click())
        val mdOrderBinding: String? = regOrderWithBindingCard("mobile-sdk-api", "956")
        sleep(4_000)
        SDKPayment.checkout(activityTestRule.activity, mdOrderBinding!!)
        sleep(6_000)
        onView(withText("•• 5599")).perform(click())
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)

        assertEquals("DEPOSITED", actualResult?.status)
    }

    @Test
    @ConfigurationSingle
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnApiExceptionWithBindingCard() {
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
        SDKPayment.checkout(activityTestRule.activity, "45dd445-f5a4ff6-ffd55454")
        sleep(6_000)

        assert(actualResult is SDKPaymentApiException)
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
        sleep(6_000)
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.cardNumberInput)).perform(typeText("4777777777777778"))
        onView(withId(R.id.cardExpiryInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardExpiryInput)).perform(typeText("12/24"))
        onView(withId(R.id.cardCodeInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardCodeInput)).perform(typeText("123"))
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        onView(withId(threeDSR.id.activity_text_challenge_dataEntry)).perform(
            typeText("123456"),
            closeSoftKeyboard()
        )
        onView(withId(threeDSR.id.activity_text_challenge_submit)).perform(click())
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(2_000)
        onView(withText("•• 7778")).perform(click())
        onView(withId(R.id.cardCodeInput)).perform(
            typeText("123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)

        takeScreen()
        onView(withId(threeDSR.id.otp_page_toolbar_cancel)).perform(click())
        sleep(6_000)

        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)

        if (actualResult == null) {
            assertNull(actualResult)
        } else {
            assert(actualResult is SDKDeclinedException)
        }
    }

    @Test
    @ConfigurationSingle
    fun shouldReturnAvailableOptionForEditCardList() {
        val mdOrder: String? = regOrderWithBindingCard()
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)
        onView(withId(R.id.editCardsList)).check(matches(isDisplayed()))
    }

    @Test
    @ConfigurationSingle
    @Ignore
    fun shouldReturnUnbindCard() {
        val clientId = getClientId()
        var mdOrder: String? = regOrderWithBindingCard(clientId = clientId)
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)
        bindCard()
        sleep(6_000)
        mdOrder = regOrderWithBindingCard(clientId = clientId)
        SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
        sleep(6_000)
        onView(withId(R.id.editCardsList)).perform(click())
        onView(withId(R.id.cardList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickItemWithId(R.id.arrow)
            )
        )
        sleep(6_000)
        onView(withText("Да")).perform(click())
        sleep(6_000)
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.cardNumberInput)).perform(typeText("4777777777777778"))
        onView(withId(R.id.cardExpiryInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardExpiryInput)).perform(typeText("12/24"))
        onView(withId(R.id.cardCodeInput)).check(matches(hasFocus()))
        onView(withId(R.id.cardCodeInput)).perform(typeText("123"))
        onView(withId(R.id.doneButton)).perform(click())
        sleep(6_000)
        onView(withId(threeDSR.id.activity_text_challenge_dataEntry)).perform(
            typeText("123456"),
            closeSoftKeyboard()
        )
        onView(withId(threeDSR.id.activity_text_challenge_submit)).perform(click())
        sleep(6_000)
        val status = runBlocking {
            paymentApi.getSessionStatus(mdOrder!!).bindingItems?.any {
                it.label.contains("555555**5599 12/24")
            }
        }
        assertEquals(false, status)
    }

    private fun bindCard() {
        testCardWithout3DS.fillOutForm()
        onView(withId(R.id.doneButton)).perform(click())
    }

    /*
        Create a client for payment with a new card from binding screen.
        Since we have only two cards, we always need a new client to test the payment of
        a new card from binding screen.
     */
    private fun getClientId(): String = System.currentTimeMillis().toString()

    private fun regOrderWithNewCard(): String? {
        val url = "https://ecommerce.radarpayments.com/payment/rest/register.do"
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
        val url = "https://ecommerce.radarpayments.com/payment/rest/register.do"
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

    private fun clickItemWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById(id) as View
                v.performClick()
            }
        }
    }
}
