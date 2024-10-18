package net.payrdr.mobile.payment.sdk.ui

import android.Manifest
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
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
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKFormsConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.ui.CardSelectedActivity
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.defaultConfig
import net.payrdr.mobile.payment.sdk.test.core.targetContext
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationRule
import net.payrdr.mobile.payment.sdk.ui.screen.SelectedCardScreen
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.runner.RunWith

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class CardSelectedActivityTest : DocLocScreenshotTestCase(
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

    private val mockCryptogramProcessor: CryptogramProcessor = mockk()

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    @get:Rule
    val activityTestRule = ActivityTestRule(CardSelectedActivity::class.java, true, false)

    private val configurationRule = ConfigurationRule()

    private val testName = TestName()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(configurationRule)
        .around(activityTestRule)
        .around(testName)

    @Before
    fun setUp() {
        SDKForms.innerCryptogramProcessor = mockCryptogramProcessor
        SDKForms.innerSdkConfig = SDKFormsConfigBuilder()
            .keyProvider(
                CachedKeyProvider(
                    RemoteKeyProvider("https://dev.bpcbt.com/payment/se/keys.do"),
                    targetContext().getSharedPreferences("key", Context.MODE_PRIVATE)
                )
            ).build()
    }

    @Test
    fun shouldRunWithCorrectLocale() {
        run {
            val config = defaultConfig()
            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "519198xxxxxx0377",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithCorrectLocale") {
                SelectedCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldRunWithCorrectLocale_1", quality = 1)
                    }
                }
            }
        }
    }

    @Test
    fun shouldNotFocusedOnCVC() {
        run {
            val config = defaultConfig()
            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "519198xxxxxx0377",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldNotFocusedOnCVC") {
                SelectedCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldNotFocusedOnCVC_1", quality = 1)
                    }
                    cardCodeInput {
                        isVisible()
                        isNotFocused()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredButtonText() {
        run {
            val config = defaultConfig().copy(buttonText = "Configured Text")
            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "519198xxxxxx0377",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredButtonText") {
                SelectedCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldRunWithConfiguredButtonText_1", quality = 1)
                        withText("Configured Text")
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    fun shouldRequireCVC() {
        run {
            val config = defaultConfig().copy(bindingCVCRequired = true)

            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "492980xxxxxx7724",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRequireCVC done button") {
                SelectedCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldRequireCVC_1", quality = 1)
                        click()
                    }
                }
            }
            step("shouldRequireCVC cvc incorrect") {
                SelectedCardScreen.doneButton {
                    isVisible()
                    click()
                }
                onView(withText(R.string.payrdr_card_incorrect_cvc))
                    .inRoot(withDecorView(not(activityTestRule.activity.window.decorView)))
                    .check(matches(isDisplayed()))
            }
            step("shouldRequireCVC verify") {
                flakySafely {
                    coVerify {
                        mockCryptogramProcessor.create(any(), any(), any(), any(), any()) wasNot called
                    }
                }
            }
        }
    }

    @Test
    fun shouldProceedValidData() {
        run {
            step("shouldProceedValidData init verify") {
                coEvery {
                    mockCryptogramProcessor.create(any(), any(), any(), any(), any())
                } returns ""
            }
            val config = defaultConfig().copy(bindingCVCRequired = true)

            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "492980xxxxxx7724",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldProceedValidData") {
                SelectedCardScreen {
                    cardCodeInput {
                        isVisible()
                        allureScreenshot(name = "shouldProceedValidData_1", quality = 1)
                        typeText("012")
                    }
                    closeSoftKeyboard()
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldProceedValidData_1", quality = 1)
                        click()
                    }
                }
            }
            step("shouldProceedValidData verify") {
                flakySafely {
                    coVerify {
                        mockCryptogramProcessor.create(
                            order = eq(config.order),
                            timestamp = eq(config.timestamp),
                            uuid = eq(config.uuid),
                            cardInfo = eq(
                                CardInfo(
                                    identifier = CardBindingIdIdentifier(
                                        value = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                                    ),
                                    cvv = "012"
                                )
                            ),
                            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
                        )
                    }
                }
            }
        }
    }

    @Test
    @Suppress("LongMethod")
    fun shouldProceedValidDataWithoutOrder() {
        run {
            step("shouldProceedValidData init verify") {
                coEvery {
                    mockCryptogramProcessor.create(
                        timestamp = any(),
                        uuid = any(),
                        cardInfo = any(),
                        registeredFrom = any(),
                    )
                } returns ""
            }
            val config = defaultConfig().copy(
                order = "",
                bindingCVCRequired = true
            )

            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "492980xxxxxx7724",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldProceedValidData") {
                SelectedCardScreen {
                    cardCodeInput {
                        isVisible()
                        allureScreenshot(name = "shouldProceedValidData_1", quality = 1)
                        typeText("012")
                    }
                    closeSoftKeyboard()
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldProceedValidData_1", quality = 1)
                        click()
                    }
                }
            }
            step("shouldProceedValidData verify") {
                flakySafely {
                    coVerify {
                        mockCryptogramProcessor.create(
                            timestamp = eq(config.timestamp),
                            uuid = eq(config.uuid),
                            cardInfo = eq(
                                CardInfo(
                                    identifier = CardBindingIdIdentifier(
                                        value = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                                    ),
                                    cvv = "012"
                                )
                            ),
                            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
                        )
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    fun shouldHideCVCInput() {
        run {
            val config = defaultConfig().copy(bindingCVCRequired = false)

            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "492980xxxxxx7724",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldHideCVCInput") {
                SelectedCardScreen {
                    cardCodeInputLayout {
                        isNotDisplayed()
                    }
                    cardCodeInput {
                        isNotDisplayed()
                    }
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldRunWithCorrectLocale_1", quality = 1)
                        click()
                    }
                }
            }
            step("shouldHideCVCInput verify") {
                flakySafely {
                    coVerify {
                        mockCryptogramProcessor.create(any(), any(), any(), any(), any())
                    }
                }
            }
        }
    }

    @Test
    fun shouldNotRequireCVC() {
        run {
            step("shouldNotRequireCVC init verify") {
                coEvery {
                    mockCryptogramProcessor.create(any(), any(), any(), any(), any())
                } returns ""
            }

            val config = defaultConfig().copy(bindingCVCRequired = false)

            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // mastercard
                    pan = "519198xxxxxx0377",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldNotRequireCVC") {
                SelectedCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldNotRequireCVC_1", quality = 1)
                        click()
                    }
                }
            }
            step("shouldNotRequireCVC verify") {
                flakySafely {
                    coVerify {
                        mockCryptogramProcessor.create(any(), any(), any(), any(), any())
                    }
                }
            }
        }
    }
}
