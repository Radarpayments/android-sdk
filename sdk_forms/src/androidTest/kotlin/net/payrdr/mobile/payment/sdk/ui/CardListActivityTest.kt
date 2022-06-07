package net.payrdr.mobile.payment.sdk.ui

import android.Manifest
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.alluresupport.addAllureSupport
import com.kaspersky.components.alluresupport.files.attachViewHierarchyToAllureReport
import com.kaspersky.kaspresso.interceptors.watcher.testcase.TestRunWatcherInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.params.ScreenshotParams
import com.kaspersky.kaspresso.testcases.api.testcase.DocLocScreenshotTestCase
import com.kaspersky.kaspresso.testcases.models.info.TestInfo
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.ui.CardListActivity
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.defaultConfig
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationRule
import net.payrdr.mobile.payment.sdk.ui.screen.CardListScreen
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.runner.RunWith

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class CardListActivityTest : DocLocScreenshotTestCase(
    kaspressoBuilder = Kaspresso.Builder.simple(
        customize = {
            screenshotParams = ScreenshotParams(quality = 1)
            if (isAndroidRuntime) {
                UiDevice
                    .getInstance(instrumentation)
                    .executeShellCommand(
                        "appops set --uid " +
                            "${InstrumentationRegistry.getInstrumentation().targetContext.packageName} " +
                            "MANAGE_EXTERNAL_STORAGE allow"
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

    @get:Rule
    val activityTestRule = ActivityTestRule(CardListActivity::class.java, true, false)

    private val configurationRule = ConfigurationRule()

    private val testName = TestName()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(configurationRule)
        .around(activityTestRule)
        .around(testName)

    @Test
    @Suppress("LongMethod")
    fun shouldRunWithLongCardList() {
        run {
            val config = defaultConfig().copy(
                cards = setOf(
                    Card( // amex
                        pan = "376839xxxxx3890",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    ),
                    Card( // jcb
                        pan = "353239xxxxxx5675",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    ),
                    Card( // maestro
                        pan = "6761831441",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    ),
                    Card( // mastercard
                        pan = "5191980377",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    ),
                    Card( // visa
                        pan = "4532559521480115",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    ),
                    Card( // mir
                        pan = "221234",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    ),
                    Card( // unknown
                        pan = "6281",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    )
                )
            )
            val launchIntent = CardListActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithLongCardList") {
                CardListScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldRunWithLongCardList_1", quality = 1)
                    }
                    bindingCard3890 {
                        isDisplayed()
                    }
                    bindingCard5675 {
                        isDisplayed()
                    }
                    bindingCard1441 {
                        isDisplayed()
                    }
                    bindingCard0377 {
                        isDisplayed()
                    }
                    swipeUp()
                    allureScreenshot(name = "shouldRunWithLongCardList_swipe_up", quality = 1)
                    bindingCard0115 {
                        isDisplayed()
                    }
                    bindingCard1234 {
                        isDisplayed()
                    }
                    bindingCard6281 {
                        isDisplayed()
                    }
                    doneButton {
                        isDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithOneCardInList() {
        run {
            val config = defaultConfig().copy(
                cards = setOf(
                    Card( // jcb
                        pan = "353239xxxxxx5675",
                        bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                    )
                )
            )
            val launchIntent = CardListActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithOneCardInList") {
                CardListScreen {
                    bindingCard5675 {
                        isDisplayed()
                        allureScreenshot(name = "shouldRunWithOneCardInList", quality = 1)
                    }
                    doneButton {
                        isDisplayed()
                    }
                }
            }
        }
    }
}
