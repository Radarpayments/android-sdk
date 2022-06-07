package net.payrdr.mobile.payment.sdk.ui.widget

import android.content.Context
import android.view.KeyEvent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputLayout
import net.payrdr.mobile.payment.sdk.form.ui.widget.CardExpiryEditText
import net.payrdr.mobile.payment.sdk.form.utils.onDisplayError
import net.payrdr.mobile.payment.sdk.test.core.CoreUIViewTest
import net.payrdr.mobile.payment.sdk.test.espresso.ExactViewMatcher.Companion.exactView
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationSingle
import org.junit.Test

@SmallTest
class CardExpiryEditTextTest : CoreUIViewTest<CardExpiryEditText>() {

    override fun prepareView(context: Context): CardExpiryEditText {
        return CardExpiryEditText(context).apply {
            showError = true
            hint = getString(R.string.payrdr_card_expiry_placeholder)
        }
    }

    override fun wrapView(context: Context, view: CardExpiryEditText): View {
        return BaseTextInputLayout(context).apply {
            addView(view)
            view onDisplayError { this.error = it }
        }
    }

    @Test
    @ConfigurationSingle
    @Description("shouldAllowInputOnlyCorrectMonthNumber")
    fun shouldAllowInputOnlyCorrectMonthNumber() {
        onView(exactView(testedView)).perform(typeText("1220"))
        allureScreenshot(name = "shouldAllowInputOnlyCorrectMonthNumber_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("12/20")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowInputMoreThenMaxLength")
    fun shouldNotAllowInputMoreThenMaxLength() {
        onView(exactView(testedView)).perform(typeText("12203"))
        allureScreenshot(name = "shouldNotAllowInputMoreThenMaxLength_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("12/20")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldAllowInputOnlyCorrectYearNumber")
    fun shouldAllowInputOnlyCorrectYearNumber() {
        onView(exactView(testedView)).perform(typeText("9920"))
        allureScreenshot(name = "shouldAllowInputOnlyCorrectYearNumber_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("99/20")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldAllowInputOnlyDigits")
    fun shouldAllowInputOnlyDigits() {
        onView(exactView(testedView)).perform(typeText("a1b1 2N0"))
        allureScreenshot(name = "shouldAllowInputOnlyDigits_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("11/20")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldShowErrorForMaxYearLimit")
    fun shouldShowErrorForMaxYearLimit() {
        onView(exactView(testedView)).perform(typeText("0159"))
        allureScreenshot(name = "shouldShowErrorForMaxYearLimit_1", quality = 1)
    }

    @Test
    @ConfigurationSingle
    @Description("shouldAppendDivider")
    fun shouldAppendDivider() {
        onView(exactView(testedView)).perform(typeText("0122"))
        allureScreenshot(name = "shouldAppendDivider_1", quality = 1)

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldAppendDivider_2", quality = 1)
        onView(exactView(testedView)).check(matches(withText("01/2")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldAppendDivider_3", quality = 1)
        onView(exactView(testedView)).check(matches(withText("01/")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldAppendDivider_4", quality = 1)
        onView(exactView(testedView)).check(matches(withText("01")))

        onView(exactView(testedView)).perform(typeText("2"))
        allureScreenshot(name = "shouldAppendDivider_5", quality = 1)
        onView(exactView(testedView)).check(matches(withText("01/2")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldWorkBackspace")
    fun shouldWorkBackspace() {
        onView(exactView(testedView)).perform(typeText("0122"))
        allureScreenshot(name = "shouldWorkBackspace_1", quality = 1)

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_2", quality = 1)
        onView(exactView(testedView)).check(matches(withText("01/2")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_3", quality = 1)
        onView(exactView(testedView)).check(matches(withText("01/")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_4", quality = 1)
        onView(exactView(testedView)).check(matches(withText("01")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_5", quality = 1)
        onView(exactView(testedView)).check(matches(withText("0")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_6", quality = 1)
        onView(exactView(testedView)).check(matches(withText("")))
    }
}
