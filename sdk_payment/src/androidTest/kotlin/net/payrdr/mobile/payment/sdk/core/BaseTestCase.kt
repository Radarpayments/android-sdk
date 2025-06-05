package net.payrdr.mobile.payment.sdk.core

import android.Manifest
import android.util.Log
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.alluresupport.addAllureSupport
import com.kaspersky.components.alluresupport.files.attachViewHierarchyToAllureReport
import com.kaspersky.kaspresso.idlewaiting.KautomatorWaitForIdleSettings
import com.kaspersky.kaspresso.interceptors.watcher.testcase.TestRunWatcherInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.params.ScreenshotParams
import com.kaspersky.kaspresso.testcases.api.testcase.DocLocScreenshotTestCase
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.kaspersky.kaspresso.testcases.models.info.TestInfo
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.data.TestClientIdHelper
import net.payrdr.mobile.payment.sdk.data.TestOrderHelper
import net.payrdr.mobile.payment.sdk.logs.Logger
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@LargeTest
@Suppress("LargeClass", "MaxLineLength")
@RunWith(AllureAndroidJUnit4::class)
open class BaseTestCase : DocLocScreenshotTestCase(
    kaspressoBuilder = Kaspresso.Builder.simple(
        customize = {
            kautomatorWaitForIdleSettings = KautomatorWaitForIdleSettings(
                waitForSelectorTimeout = 20_000,
                waitForIdleTimeout = 20_000,
            )
            screenshotParams = ScreenshotParams(quality = 1)
            if (isAndroidRuntime) {
                UiDevice
                    .getInstance(instrumentation)
                    .executeShellCommand(
                        "appops set --uid ${InstrumentationRegistry.getInstrumentation().targetContext.packageName} MANAGE_EXTERNAL_STORAGE allow"
                    )
            }
        },
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

    @get:Rule
    val activityTestRule = ActivityTestRule(TestActivity::class.java, true, false)

    private val resultHandlerHelper get() = activityTestRule.activity.resultHandlerHelper

    protected lateinit var testOrderHelper: TestOrderHelper
    protected lateinit var testClientIdHelper: TestClientIdHelper
    protected val testActivity get() = activityTestRule.activity
    protected lateinit var testPaymentConfig: SDKPaymentConfig

    @Before
    fun setUp() {
        Logger.addLogInterface(TestLoggerFactory.createLogcatLogger("TESTLOG"))
        val baseUrl = "https://dev.bpcbt.com/payment"
        testPaymentConfig = SDKPaymentConfig(baseUrl)
        SDKPayment.getSDKVersion()
        activityTestRule.launchActivity(null)
        testOrderHelper = TestOrderHelper(activityTestRule.activity.applicationContext, baseUrl)
        testClientIdHelper = TestClientIdHelper(startClientId = System.currentTimeMillis())

        resultHandlerHelper.resetPaymentData()
    }

    internal fun TestContext<*>.verifyResult(action: ResultHandlerHelper.() -> Unit) {
        try {
            flakySafely(
                timeoutMs = 20_000,
                intervalMs = 1_000,
            ) {
                action(resultHandlerHelper)
            }
            printResultHandlerStatus(resultHandlerHelper)
        } catch (th: Throwable) {
            printResultHandlerStatus(resultHandlerHelper)
            throw th
        }
    }

    private fun printResultHandlerStatus(resultHandlerHelper: ResultHandlerHelper) {
        Log.d(
            "TESTLOG", "verifyResult paymentData ${resultHandlerHelper.paymentData}"
        )
    }
}
