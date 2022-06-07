package net.payrdr.mobile.payment.sdk.ui.widget

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.widget.BankCardView
import net.payrdr.mobile.payment.sdk.test.SleepEmulator.sleep
import net.payrdr.mobile.payment.sdk.test.core.CoreUIViewTest
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationSingle
import org.junit.Test

@SmallTest
class BankCardViewTest : CoreUIViewTest<BankCardView>() {

    override fun prepareView(context: Context): BankCardView {
        return BankCardView(context).apply {
            setupUnknownBrand()
        }
    }

    @Test
    @ConfigurationSingle
    @Description("shouldDisplayNumberMask")
    fun shouldDisplayNumberMask() {
        allureScreenshot(name = "shouldDisplayNumberMask_1", quality = 1)

        activityTestRule.runOnUiThread {
            testedView.setNumber("123")
        }
        allureScreenshot(name = "shouldDisplayNumberMask_2", quality = 1)
        onView(withId(R.id.cardNumber)).check(matches(withText("123• •••• •••• ••••  ")))

        activityTestRule.runOnUiThread {
            testedView.setNumber("123456")
        }
        allureScreenshot(name = "shouldDisplayNumberMask_3", quality = 1)
        onView(withId(R.id.cardNumber)).check(matches(withText("1234 56•• •••• ••••  ")))

        activityTestRule.runOnUiThread {
            testedView.setNumber("1234567890123456")
        }
        allureScreenshot(name = "shouldDisplayNumberMask_4", quality = 1)
        onView(withId(R.id.cardNumber)).check(matches(withText("1234 5678 9012 3456  ")))

        activityTestRule.runOnUiThread {
            testedView.setNumber("1234567890123456789")
        }
        allureScreenshot(name = "shouldDisplayNumberMask_5", quality = 1)
        onView(withId(R.id.cardNumber)).check(matches(withText("1234 5678 9012 3456789")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldDisplayExpireMask")
    fun shouldDisplayExpireMask() {
        allureScreenshot(name = "shouldDisplayExpireMask_1", quality = 1)
        onView(withId(R.id.cardExpiry))
            .check(matches(withText("••/••")))

        activityTestRule.runOnUiThread {
            testedView.setExpiry("12")
        }
        allureScreenshot(name = "shouldDisplayExpireMask_2", quality = 1)
        onView(withId(R.id.cardExpiry)).check(matches(withText("12/••")))

        activityTestRule.runOnUiThread {
            testedView.setExpiry("12/25")
        }
        allureScreenshot(name = "shouldDisplayExpireMask_3", quality = 1)
        onView(withId(R.id.cardExpiry)).check(matches(withText("12/25")))

        activityTestRule.runOnUiThread {
            testedView.setExpiry("55555")
        }
        allureScreenshot(name = "shouldDisplayExpireMask_4", quality = 1)
        onView(withId(R.id.cardExpiry)).check(matches(withText("55/55")))

        activityTestRule.runOnUiThread {
            testedView.setExpiry("123456789")
        }
        allureScreenshot(name = "shouldDisplayExpireMask_5", quality = 1)
        sleep()
        onView(withId(R.id.cardExpiry)).check(matches(withText("12/34")))
    }
}
