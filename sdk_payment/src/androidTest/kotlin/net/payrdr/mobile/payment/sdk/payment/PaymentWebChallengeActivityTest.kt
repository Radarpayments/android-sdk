package net.payrdr.mobile.payment.sdk.payment

import android.Manifest
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
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
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.PaymentApiImpl
import net.payrdr.mobile.payment.sdk.core.TestActivity
import net.payrdr.mobile.payment.sdk.form.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.utils.executePostParams
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@LargeTest
@Suppress("LargeClass", "MaxLineLength")
@RunWith(AllureAndroidJUnit4::class)
class PaymentWebChallengeActivityTest : DocLocScreenshotTestCase(
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
        val cvc: String
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

    private val testCardWith3DSWithPaRes = TestCard(
        pan = "4012001038166662",
        expiry = "12/24",
        cvc = "123"
    )

    private val testCardWith3DS = TestCard(
        pan = "5000001111111115",
        expiry = "12/30",
        cvc = "123"
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
        SDKPayment.init(paymentConfig, false)
        SDKPayment.getSDKVersion()
        activityTestRule.launchActivity(null)
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCard() {
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

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCard Open Payment BottomSheet Screen") {
                val mdOrder: String? = regOrderWithNewCard()
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCard Open New Card Screen") {
                BottomSheetScreen {
                    Thread.sleep(5000L)
                    addNewCard {
                        click()
                    }
                }
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCard Fill Out Form") {
                testCardWith3DS.fillOutForm()
                NewCardScreen {
                    closeSoftKeyboard()
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCard WebView") {
                Thread.sleep(5000L)
                onWebView(withId(R.id.web_view))
                    .forceJavascriptEnabled()
                    .withElement(
                        findElement(
                            Locator.XPATH,
                            "/html/body/div/div[2]/div[1]/button[1]"
                        )
                    )
                    .perform(webClick())
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCard Assert") {
                flakySafely {
                    Assert.assertEquals("DEPOSITED", actualResult?.status)
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Suppress("EmptyFunctionBlock")
    fun shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCardPaReq() {
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

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCardPaReq Open Payment BottomSheet Screen") {
                val mdOrder: String? = regOrderWithNewCard()
                SDKPayment.checkout(activityTestRule.activity, mdOrder!!)
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCardPaReq Open New Card Screen") {
                BottomSheetScreen {
                    Thread.sleep(5000L)
                    addNewCard {
                        click()
                    }
                }
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCardPaReq Fill Out Form") {
                testCardWith3DSWithPaRes.fillOutForm()
                NewCardScreen {
                    closeSoftKeyboard()
                    doneButton {
                        isVisible()
                        click()
                    }
                }
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCardPaReq WebView") {
                Thread.sleep(5000L)
                onWebView(withId(R.id.web_view))
                    .forceJavascriptEnabled()
                    .withElement(findElement(Locator.XPATH, "/html/body/main/form/div/input"))
                    .perform(webKeys("12345678"))
            }

            step("shouldReturnSuccessPaymentDataWithThreeDsVer1WithNewCardPaReq Assert") {
                flakySafely {
                    Assert.assertEquals("DEPOSITED", actualResult?.status)
                }
            }
        }
    }

    private fun regOrderWithNewCard(): String? {
        val url = "https://dev.bpcbt.com/payment/rest/register.do"
        val body = mapOf(
            "amount" to "20000",
            "userName" to "mobile-sdk-api",
            "password" to "vkyvbG0",
            "returnUrl" to "sdk://done"
        )

        return runCatching {
            val connection = URL(url).executePostParams(body)
            connection.responseBodyToJsonObject().getString("orderId")
        }.getOrNull()
    }
}
