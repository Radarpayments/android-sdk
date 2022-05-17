package net.payrdr.mobile.payment.sdk.ui

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.ui.CardSelectedActivity
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.defaultConfig
import net.payrdr.mobile.payment.sdk.test.SleepEmulator.sleep
import net.payrdr.mobile.payment.sdk.test.core.CoreUITest
import net.payrdr.mobile.payment.sdk.test.core.targetContext
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@SmallTest
class CardSelectedActivityTest :
    CoreUITest<CardSelectedActivity>(CardSelectedActivity::class.java, true, false) {

    private val mockCryptogramProcessor: CryptogramProcessor = mockk()

    @Before
    fun setUp() {
        SDKForms.innerCryptogramProcessor = mockCryptogramProcessor
        SDKForms.innerSdkConfig = SDKConfigBuilder()
            .keyProvider(
                CachedKeyProvider(
                    RemoteKeyProvider("https://ecommerce.radarpayments.com/payment/se/keys.do"),
                    targetContext().getSharedPreferences("key", Context.MODE_PRIVATE)
                )
            ).build()
    }

    @Test
    fun shouldRunWithCorrectLocale() {
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
        sleep()
        takeScreen()
    }

    @Test
    fun shouldNotFocusedOnCVC() {
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
        takeScreen()

        onView(withId(R.id.cardCodeInput)).check(matches(not(hasFocus())))
    }

    @Test
    fun shouldRunWithConfiguredButtonText() {
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
        sleep()
        takeScreen()

        onView(withId(R.id.doneButton)).check(matches(withText("Configured Text")))
    }

    @Test
    fun shouldRequireCVC() {
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
        sleep()
        takeScreen()

        onView(withId(R.id.doneButton)).perform(click())
        onView(withText(R.string.payrdr_card_incorrect_cvc)).inRoot(withDecorView(not(getActivity().window.decorView)))
            .check(matches(isDisplayed()))

        takeScreen()

        coVerify {
            mockCryptogramProcessor.create(any(), any(), any(), any()) wasNot called
        }
    }

    @Test
    fun shouldProceedValidData() {
        coEvery {
            mockCryptogramProcessor.create(any(), any(), any(), any())
        } returns ""

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
        sleep()
        takeScreen()
        onView(withId(R.id.cardCodeInput)).perform(typeText("012"))
        takeScreen()
        onView(withId(R.id.doneButton)).perform(click())

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
                )
            )
        }
    }

    @Test
    @Ignore
    fun shouldHideCVCInput() {
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
        sleep()

        onView(withId(R.id.cardCodeInputLayout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cardCodeInput)).check(matches(not(isDisplayed())))
        onView(withId(R.id.doneButton)).perform(click())

        takeScreen()
        coVerify {
            mockCryptogramProcessor.create(any(), any(), any(), any())
        }
    }

    @Test
    fun shouldNotRequireCVC() {
        coEvery {
            mockCryptogramProcessor.create(any(), any(), any(), any())
        } returns ""

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
        sleep()
        takeScreen()

        onView(withId(R.id.doneButton)).perform(click())

        coVerify {
            mockCryptogramProcessor.create(any(), any(), any(), any())
        }
    }
}
