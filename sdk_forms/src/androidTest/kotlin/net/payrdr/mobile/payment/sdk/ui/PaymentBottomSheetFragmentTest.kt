package net.payrdr.mobile.payment.sdk.ui

import android.Manifest
import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
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
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.SDKFormsConfigBuilder
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.HolderInputOptions
import net.payrdr.mobile.payment.sdk.form.ui.PaymentBottomSheetFragment
import net.payrdr.mobile.payment.sdk.form.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.form.ui.helper.ThemeSetting
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider
import net.payrdr.mobile.payment.sdk.test.core.TestActivity
import net.payrdr.mobile.payment.sdk.test.core.targetContext
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationRule
import net.payrdr.mobile.payment.sdk.ui.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.ui.screen.PaymentBottomSheetScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.runner.RunWith

@SmallTest
@Suppress("LargeClass")
@RunWith(AllureAndroidJUnit4::class)
class PaymentBottomSheetFragmentTest : DocLocScreenshotTestCase(
    kaspressoBuilder = Kaspresso.Builder.simple(
        customize = {
            screenshotParams = ScreenshotParams(quality = 1)
            if (isAndroidRuntime) {
                UiDevice
                    .getInstance(instrumentation)
                    .executeShellCommand(
                        "appops set --uid " +
                                "${InstrumentationRegistry.getInstrumentation().targetContext.packageName}" +
                                " MANAGE_EXTERNAL_STORAGE allow"
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
    val activityRule = ActivityScenarioRule(TestActivity::class.java)

    private val configurationRule = ConfigurationRule()

    private val testName = TestName()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(configurationRule)
        .around(activityRule)
        .around(testName)

    @Before
    fun setUp() {
        SDKForms.init(
            SDKFormsConfigBuilder()
                .keyProvider(
                    CachedKeyProvider(
                        RemoteKeyProvider("https://dev.bpcbt.com/payment/se/keys.do"),
                        targetContext().getSharedPreferences("key", Context.MODE_PRIVATE)
                    )
                ).build()
        )
    }

    @Test
    fun shouldRunWithCardList() {
        run {
            val cards = setOf(
                Card(
                    "492980xxxxxx7724", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                    ExpiryDate(2025, 12)
                ),
                Card(
                    "558620xxxxxx6614", "6617c0b1-9976-45d9-b659-364ecac099e2",
                    ExpiryDate(2024, 6)
                ),
                Card(
                    "415482xxxxxx0000", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                    ExpiryDate(2019, 1)
                ),
                Card("411790xxxxxx123456", "ceae68c1-cb02-4804-9526-6d6b2f1f2793")
            )
            val config = PaymentConfigTestProvider.defaultConfig().copy(
                holderInputOptions = HolderInputOptions.VISIBLE,
                cardSaveOptions = CardSaveOptions.NO_BY_DEFAULT,
                cards = cards
            )
            activityRule.scenario.onActivity { activity ->
                PaymentBottomSheetFragment().show(activity.supportFragmentManager, null, config)
            }

            step("shouldRunWithCardList") {
                PaymentBottomSheetScreen {
                    dismissButton.isClickable()

                    bindingCard0000 {
                        isDisplayed()
                    }
                    bindingCard7724 {
                        isDisplayed()
                    }
                    bindingCard3456 {
                        isDisplayed()
                    }
                    bindingCard6614 {
                        isDisplayed()
                    }
                    addNewCardText {
                        isDisplayed()
                    }
                    allPaymentMethods {
                        isDisplayed()
                        allureScreenshot(name = "shouldRunWithCardList", quality = 1)
                    }
                }
            }
        }
    }

    @Test
    fun shouldOpenNewCardScreen() {
        run {
            val config = PaymentConfigTestProvider.defaultConfig().copy(
                holderInputOptions = HolderInputOptions.VISIBLE,
                cardSaveOptions = CardSaveOptions.NO_BY_DEFAULT
            )
            LocalizationSetting.setLanguage(config.locale)
            ThemeSetting.setTheme(config.theme)
            activityRule.scenario.onActivity { activity ->
                PaymentBottomSheetFragment().show(activity.supportFragmentManager, null, config)
            }
            step("ClickOnAddNewCard") {
                PaymentBottomSheetScreen {
                    Thread.sleep(2000)
                    addNewCardText {
                        click()
                    }
                }
            }
            step("ShouldOpenNewCardScreen") {
                NewCardScreen {
                    Thread.sleep(2000)
                    cardNumberInput {
                        isDisplayed()
                        allureScreenshot(name = "shouldOpenNewCardScreen", quality = 1)
                    }
                    cardExpiryInput {
                        isDisplayed()
                    }
                    cardCodeInput {
                        isDisplayed()
                    }
                }
            }
        }
    }
}
