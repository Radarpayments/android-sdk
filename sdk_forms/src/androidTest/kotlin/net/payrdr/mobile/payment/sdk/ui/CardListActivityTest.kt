package net.payrdr.mobile.payment.sdk.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.ui.CardListActivity
import net.payrdr.mobile.payment.sdk.test.PaymentConfigTestProvider.defaultConfig
import net.payrdr.mobile.payment.sdk.test.SleepEmulator.sleep
import net.payrdr.mobile.payment.sdk.test.core.CoreUITest
import org.junit.Test

@SmallTest
class CardListActivityTest :
    CoreUITest<CardListActivity>(CardListActivity::class.java, true, false) {

    @Test
    fun shouldRunWithLongCardList() {
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
        takeScreen()

        onView(withText("•• 3890")).check(matches(isDisplayed()))
        onView(withText("•• 5675")).check(matches(isDisplayed()))
        onView(withText("•• 1441")).check(matches(isDisplayed()))
        onView(withText("•• 0377")).check(matches(isDisplayed()))

        onView(withId(R.id.cardList)).perform(swipeUp())
        sleep()
        takeScreen("swipeUp")

        onView(withText("•• 0115")).check(matches(isDisplayed()))
        onView(withText("•• 1234")).check(matches(isDisplayed()))
        onView(withText("•• 6281")).check(matches(isDisplayed()))
        onView(withId(R.id.doneButton)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldRunWithOneCardInList() {
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
        takeScreen()

        onView(withText("•• 5675")).check(matches(isDisplayed()))
        onView(withId(R.id.doneButton)).check(matches(isDisplayed()))
        sleep()
    }
}
