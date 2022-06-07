package net.payrdr.mobile.payment.sdk.ui

import android.Manifest
import android.content.Context
import android.view.KeyEvent
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.HolderInputOptions
import net.payrdr.mobile.payment.sdk.form.ui.CardNewActivity
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.defaultConfig
import net.payrdr.mobile.payment.sdk.test.core.getString
import net.payrdr.mobile.payment.sdk.test.core.targetContext
import net.payrdr.mobile.payment.sdk.test.espresso.TextInputLayoutErrorTextMatcher.Companion.hasTextInputLayoutHintText
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationRule
import net.payrdr.mobile.payment.sdk.ui.screen.NewCardScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.runner.RunWith

@SmallTest
@Suppress("LargeClass")
@RunWith(AllureAndroidJUnit4::class)
class CardNewActivityTest : DocLocScreenshotTestCase(
    kaspressoBuilder = Kaspresso.Builder.simple(
        customize = {
            screenshotParams = ScreenshotParams(quality = 1)
            if (isAndroidRuntime) {
                UiDevice
                    .getInstance(instrumentation)
                    .executeShellCommand(
                        "appops set --uid" +
                            " ${InstrumentationRegistry.getInstrumentation().targetContext.packageName}" +
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
    val activityTestRule = ActivityTestRule(CardNewActivity::class.java, true, false)

    private val configurationRule = ConfigurationRule()

    private val testName = TestName()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(configurationRule)
        .around(activityTestRule)
        .around(testName)

    @Before
    fun setUp() {
        SDKForms.init(
            SDKConfigBuilder()
                .keyProvider(
                    CachedKeyProvider(
                        RemoteKeyProvider("https://ecommerce.radarpayments.com/payment/se/keys.do"),
                        targetContext().getSharedPreferences("key", Context.MODE_PRIVATE)
                    )
                ).build()
        )
        SDKForms.innerCryptogramProcessor = mockCryptogramProcessor
    }

    @Test
    fun shouldRunWithCorrectLocale() {
        run {
            val config = defaultConfig()
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithCorrectLocale") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldRunWithCorrectLocale_1", quality = 1)
                    }
                }
            }
        }
    }

    @Test
    fun shouldScrollWithSoftwareKeyboard() {
        run {
            val config = defaultConfig().copy(
                holderInputOptions = HolderInputOptions.VISIBLE,
                cardSaveOptions = CardSaveOptions.NO_BY_DEFAULT
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldScrollWithSoftwareKeyboard") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldScrollWithSoftwareKeyboard_1", quality = 1)
                    }
                    cardHolderInput {
                        isDisplayed()
                    }
                    checkSaveCard {
                        isDisplayed()
                    }
                    cardCodeInput {
                        typeText("123")
                    }
                    closeSoftKeyboard()
                    swipeUp()
                    doneButton {
                        click()
                    }
                    swipeUp()
                    flakySafely {
                        doneButton {
                            isDisplayed()
                        }
                    }
                    cardNumberInput {
                        typeText("123")
                    }
                    click()
                    swipeUp()
                    doneButton {
                        isDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun shouldAutoJumpToNextInput() {
        run {
            val config = defaultConfig().copy(
                holderInputOptions = HolderInputOptions.VISIBLE,
                cardSaveOptions = CardSaveOptions.NO_BY_DEFAULT
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldAutoJumpToNextInput") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldAutoJumpToNextInput_1", quality = 1)
                    }
                    cardNumberInput {
                        typeText("4594929153014323")
                    }
                    cardExpiryInput {
                        hasFocus()
                        typeText("12/27")
                    }
                    cardCodeInput {
                        hasFocus()
                        typeText("123")
                    }
                    cardHolderInput {
                        hasFocus()
                        typeText("JOHN")
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredButtonText() {
        run {
            val config = defaultConfig().copy(buttonText = "Configured Text")
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredButtonText") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(name = "shouldRunWithConfiguredButtonText_1", quality = 1)
                        withText("Configured Text")
                        isDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredSaveCardHide() {
        run {
            val config = defaultConfig().copy(cardSaveOptions = CardSaveOptions.HIDE)
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    checkSaveCard {
                        isNotDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredSaveCardYesByDefault() {
        run {
            val config = defaultConfig().copy(cardSaveOptions = CardSaveOptions.YES_BY_DEFAULT)
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    checkSaveCard {
                        isDisplayed()
                        isChecked()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredHolderInputHide() {
        run {
            val config = defaultConfig().copy(holderInputOptions = HolderInputOptions.HIDE)
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    cardHolderInput {
                        isNotDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredHolderInputHideAndCardSaveHideAndConfiguredButton() {
        run {
            val config = defaultConfig().copy(
                holderInputOptions = HolderInputOptions.HIDE,
                cardSaveOptions = CardSaveOptions.HIDE,
                buttonText = "Fast payment"
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    cardHolderInput {
                        isNotDisplayed()
                    }
                    checkSaveCard {
                        isNotDisplayed()
                    }
                    doneButton {
                        withText("Fast payment")
                        isDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredHolderInputVisible() {
        run {
            val config = defaultConfig().copy(holderInputOptions = HolderInputOptions.VISIBLE)
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    cardHolderInput {
                        isDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun shouldRunWithConfiguredSaveCardNoByDefault() {
        run {
            val config = defaultConfig().copy(
                cardSaveOptions = CardSaveOptions.NO_BY_DEFAULT
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    checkSaveCard {
                        isDisplayed()
                        isNotChecked()
                    }
                }
            }
        }
    }

    @Test
    @Suppress("LongMethod")
    fun shouldProceedValidData() {
        run {
            val config = defaultConfig().copy(
                holderInputOptions = HolderInputOptions.VISIBLE
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("") {
                coEvery {
                    mockCryptogramProcessor.create(any(), any(), any(), any())
                } returns ""
            }
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    cardNumberInput {
                        typeText("5586200016956614")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardExpiryInput {
                        typeText("1225")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardCodeInput {
                        typeText("012")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardHolderInput {
                        typeText("KONSTANTINOPOLSKY")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    doneButton {
                        click()
                    }
                }
            }
            step("") {
                coVerify {
                    mockCryptogramProcessor.create(
                        order = eq(config.order),
                        timestamp = eq(config.timestamp),
                        uuid = eq(config.uuid),
                        cardInfo = eq(
                            CardInfo(
                                identifier = CardPanIdentifier(
                                    value = "5586200016956614"
                                ),
                                expDate = ExpiryDate(
                                    expYear = 2025,
                                    expMonth = 12
                                ),
                                cvv = "012"
                            )
                        )
                    )
                }
            }
        }
    }

    @Test
    @Suppress("LongMethod")
    fun shouldProceedValidDataWithoutOrder() {
        run {
            val config = defaultConfig().copy(
                order = "",
                holderInputOptions = HolderInputOptions.VISIBLE
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("") {
                coEvery {
                    mockCryptogramProcessor.create(any(), any(), any(), any())
                } returns ""
            }
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    doneButton {
                        isVisible()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    cardNumberInput {
                        typeText("5586200016956614")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardExpiryInput {
                        typeText("1225")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardCodeInput {
                        typeText("012")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardHolderInput {
                        typeText("KONSTANTINOPOLSKY")
                    }
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    doneButton {
                        click()
                    }
                }
            }
            step("") {
                coVerify {
                    mockCryptogramProcessor.create(
                        timestamp = eq(config.timestamp),
                        uuid = eq(config.uuid),
                        cardInfo = eq(
                            CardInfo(
                                identifier = CardPanIdentifier(
                                    value = "5586200016956614"
                                ),
                                expDate = ExpiryDate(
                                    expYear = 2025,
                                    expMonth = 12
                                ),
                                cvv = "012"
                            )
                        )
                    )
                }
            }
        }
    }

    @Test
    fun shouldDisplayCardLengthError() {
        run {
            val config = defaultConfig()
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    cardNumberInput {
                        typeText("55862000")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    doneButton {
                        click()
                    }
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardNumberInputLayout {
                        hasTextInputLayoutHintText(
                            getString(R.string.payrdr_card_incorrect_length)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun shouldDisplayCodeError() {
        run {
            val config = defaultConfig()
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    cardCodeInput {
                        typeText("12")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    doneButton {
                        click()
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    cardCodeInputLayout {
                        hasTextInputLayoutHintText(
                            getString(R.string.payrdr_card_incorrect_cvc)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun shouldDisplayExpireError() {
        run {
            val config = defaultConfig()
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    cardExpiryInput {
                        typeText("55")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    doneButton {
                        click()
                    }
                    cardExpiryInputLayout {
                        hasTextInputLayoutHintText(
                            getString(R.string.payrdr_card_incorrect_expiry)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun shouldDisplayAllErrors() {
        run {
            val config = defaultConfig()
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldRunWithConfiguredSaveCardHide") {
                NewCardScreen {
                    cardNumberInput {
                        typeText("55862000")
                        allureScreenshot(
                            name = "shouldRunWithConfiguredSaveCardHide_1",
                            quality = 1
                        )
                    }
                    closeSoftKeyboard()
                    cardCodeInput {
                        typeText("12")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardExpiryInput {
                        typeText("55")
                    }
                    closeSoftKeyboard()
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    doneButton {
                        click()
                    }
                    allureScreenshot(name = "shouldRunWithConfiguredSaveCardHide_1", quality = 1)
                    cardNumberInputLayout {
                        hasTextInputLayoutHintText(
                            getString(R.string.payrdr_card_incorrect_length)
                        )
                    }
                    cardCodeInputLayout {
                        hasTextInputLayoutHintText(
                            getString(R.string.payrdr_card_incorrect_cvc)
                        )
                    }
                    cardExpiryInputLayout {
                        hasTextInputLayoutHintText(
                            getString(R.string.payrdr_card_incorrect_expiry)
                        )
                    }
                }
            }
        }
    }
}
