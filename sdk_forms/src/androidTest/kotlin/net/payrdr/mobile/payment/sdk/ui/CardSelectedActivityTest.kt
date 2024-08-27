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
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.SDKFormsConfigBuilder
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.model.AdditionalField
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.ui.CardSelectedActivity
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.configWithAllAdditionalCardParams
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.defaultConfig
import net.payrdr.mobile.payment.sdk.test.core.getString
import net.payrdr.mobile.payment.sdk.test.core.targetContext
import net.payrdr.mobile.payment.sdk.test.espresso.TextInputLayoutErrorTextMatcher.Companion.hasTextInputLayoutHintText
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
            val config = defaultConfig().copy(storedPaymentMethodCVCRequired = true)

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
        }
    }

    @Test
    fun shouldProceedValidData() {
        run {
            val config = defaultConfig().copy(storedPaymentMethodCVCRequired = true)

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
        }
    }

    @Test
    @Suppress("LongMethod")
    fun shouldProceedValidDataWithoutOrder() {
        run {
            val config = defaultConfig().copy(
                order = "",
                storedPaymentMethodCVCRequired = true
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
        }
    }

    @Test
    @Ignore
    fun shouldHideCVCInput() {
        run {
            val config = defaultConfig().copy(storedPaymentMethodCVCRequired = false)

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
        }
    }

    @Test
    fun shouldNotRequireCVC() {
        run {

            val config = defaultConfig().copy(storedPaymentMethodCVCRequired = false)

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
        }
    }

    @Test
    fun shouldShowAllAdditionalCardParamsWithHintForSavedCard() {
        run {
            val config = configWithAllAdditionalCardParams()
            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // visa
                    pan = "492980xxxxxx7724",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldShowAllAdditionalCardParamsWithHintForSavedCard") {
                SelectedCardScreen {
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
    fun shouldShowErrorOnlyForMandatoryAdditionalCardParamsForSavedCard() {
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
            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // visa
                    pan = "492980xxxxxx7724",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldShowErrorOnlyForMandatoryAdditionalCardParamsForSavedCard") {
                SelectedCardScreen {
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
    fun shouldShowAdditionalCardParamsWithPrefilledValuesForSavedCard() {
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
            val launchIntent = CardSelectedActivity.prepareIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                config,
                Card( // visa
                    pan = "492980xxxxxx7724",
                    bindingId = "0a72fe5e-ffb7-44f6-92df-8787e8a8f440"
                )
            )
            activityTestRule.launchActivity(launchIntent)
            step("shouldShowAdditionalCardParamsWithPrefilledValuesForSavedCard") {
                SelectedCardScreen {
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
