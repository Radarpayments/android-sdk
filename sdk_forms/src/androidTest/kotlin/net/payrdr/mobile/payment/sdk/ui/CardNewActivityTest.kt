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
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.SDKFormsConfigBuilder
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.model.AdditionalField
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.HolderInputOptions
import net.payrdr.mobile.payment.sdk.form.ui.CardNewActivity
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.configWithAllAdditionalCardParams
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
                cardSaveOptions = CardSaveOptions.NO_BY_DEFAULT,
                fieldsNeedToBeFilledForVisa = listOf(
                    AdditionalField(
                        fieldName = "MOBILE_PHONE",
                        isMandatory = true,
                        prefilledValue = null
                    )
                )
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
                    phoneNumberInput {
                        isVisible()
                    }
                    cardCodeInput {
                        hasFocus()
                        typeText("123")
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
                    cardHolderInput.isDisplayed()
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
                    mockCryptogramProcessor.create(any(), any(), any(), any(), any())
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
                    cardHolderInput {
                        typeText("MASHA")
                    }
                    closeSoftKeyboard()
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
                                cvv = "012",
                                cardHolder = "MASHA"
                            )
                        ),
                        registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
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
                    mockCryptogramProcessor.create(any(), any(), any(), any(), any())
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
                    cardHolderInput {
                        typeText("MASHA")
                    }
                    closeSoftKeyboard()
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
                                cvv = "012",
                                cardHolder = "MASHA"
                            )
                        ),
                        registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
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
            val config = defaultConfig().copy(holderInputOptions = HolderInputOptions.VISIBLE)
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
                    cardHolderInput {
                        hasTextInputLayoutHintText(
                            getString(R.string.payrdr_card_incorrect_card_holder)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun shouldShowAllAdditionalCardParamsWithHint() {
        run {
            val config = configWithAllAdditionalCardParams()
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldShowAllAdditionalCardParamsWithHint") {
                NewCardScreen {
                    cardNumberInput {
                        typeText("4594929153014323")
                    }
                    phoneNumberInput {
                        isVisible()
                        hasHint(R.string.payrdr_phone_number)
                    }
                    emailInput {
                        isVisible()
                        hasHint(R.string.payrdr_email)
                    }
                    cityInput {
                        isVisible()
                        hasHint(R.string.payrdr_city)
                    }
                    countryInput {
                        isVisible()
                        hasHint(R.string.payrdr_country)
                    }
                    postalCodeInput {
                        isVisible()
                        hasHint(R.string.payrdr_postal_code)
                    }
                    stateInput {
                        isVisible()
                        hasHint(R.string.payrdr_state)
                    }
                    addressLine1Input {
                        isVisible()
                        hasHint(R.string.payrdr_address_line_1)
                    }
                    addressLine2Input {
                        isVisible()
                        hasHint(R.string.payrdr_address_line_2)
                    }
                    addressLine3Input {
                        isVisible()
                        hasHint(R.string.payrdr_address_line_3)
                    }
                }
            }
        }
    }

    @Test
    fun shouldShowErrorOnlyForMandatoryAdditionalCardParams() {
        run {
            val config = defaultConfig().copy(
                fieldsNeedToBeFilledForVisa = listOf(
                    AdditionalField(
                        fieldName = "MOBILE_PHONE",
                        isMandatory = true,
                        prefilledValue = null
                    ),
                    AdditionalField(fieldName = "EMAIL", isMandatory = true, prefilledValue = null),
                    AdditionalField(
                        fieldName = "BILLING_CITY",
                        isMandatory = false,
                        prefilledValue = null
                    ),
                    AdditionalField(
                        fieldName = "BILLING_COUNTRY",
                        isMandatory = true,
                        prefilledValue = null
                    ),
                )
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldShowErrorOnlyForMandatoryAdditionalCardParams") {
                NewCardScreen {
                    cardNumberInput {
                        typeText("4594929153014323")
                    }
                    cardExpiryInput {
                        typeText("0628")
                    }
                    cardCodeInput {
                        typeText("235")
                    }
                    closeSoftKeyboard()
                    doneButton {
                        scrollTo()
                        click()
                    }
                    phoneNumberInputLayout {
                        hasTextInputLayoutHintText(getString(R.string.payrdr_not_empty_required))
                    }
                    emailInputLayout {
                        hasTextInputLayoutHintText(getString(R.string.payrdr_not_empty_required))
                    }
                    cityInputLayout {
                        hasNoError()
                    }
                    countryInputLayout {
                        hasTextInputLayoutHintText(getString(R.string.payrdr_not_empty_required))
                    }
                }
            }
        }
    }

    @Test
    fun shouldShowAdditionalCardParamsWithPrefilledValues() {
        run {
            val config = defaultConfig().copy(
                fieldsNeedToBeFilledForVisa = listOf(
                    AdditionalField(
                        fieldName = "MOBILE_PHONE",
                        isMandatory = true,
                        prefilledValue = "88005553535"
                    ),
                    AdditionalField(
                        fieldName = "BILLING_ADDRESS_LINE1",
                        isMandatory = false,
                        prefilledValue = "Baker street"
                    )
                )
            )
            val launchIntent = CardNewActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldShowAdditionalCardParamsWithPrefilledValues") {
                NewCardScreen {
                    cardNumberInput {
                        typeText("4594929153014323")
                    }
                    closeSoftKeyboard()
                    phoneNumberInput {
                        isVisible()
                        hasText("88005553535")
                    }
                    emailInput {
                        isNotDisplayed()
                    }
                    cityInput {
                        isNotDisplayed()
                    }
                    countryInput {
                        isNotDisplayed()
                    }
                    postalCodeInput {
                        isNotDisplayed()
                    }
                    stateInput {
                        isNotDisplayed()
                    }
                    addressLine1Input {
                        hasText("Baker street")
                    }
                    addressLine2Input {
                        isNotDisplayed()
                    }
                    addressLine3Input {
                        isNotDisplayed()
                    }
                }
            }
        }
    }
}
