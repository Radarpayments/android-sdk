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
    fun shouldAllowInputOnlyCorrectMonthNumber() {
        onView(exactView(testedView)).perform(typeText("1220"))
        takeScreen()

        onView(exactView(testedView)).check(matches(withText("12/20")))
    }

    @Test
    @ConfigurationSingle
    fun shouldNotAllowInputMoreThenMaxLength() {
        onView(exactView(testedView)).perform(typeText("12203"))
        takeScreen()

        onView(exactView(testedView)).check(matches(withText("12/20")))
    }

    @Test
    @ConfigurationSingle
    fun shouldAllowInputOnlyCorrectYearNumber() {
        onView(exactView(testedView)).perform(typeText("9920"))
        takeScreen()

        onView(exactView(testedView)).check(matches(withText("99/20")))
    }

    @Test
    @ConfigurationSingle
    fun shouldAllowInputOnlyDigits() {
        onView(exactView(testedView)).perform(typeText("a1b1 2N0"))
        takeScreen()

        onView(exactView(testedView)).check(matches(withText("11/20")))
    }

    @Test
    @ConfigurationSingle
    fun shouldShowErrorForMaxYearLimit() {
        onView(exactView(testedView)).perform(typeText("0159"))
        takeScreen()
    }

    @Test
    @ConfigurationSingle
    fun shouldAppendDivider() {
        onView(exactView(testedView)).perform(typeText("0122"))
        takeScreen()

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("01/2")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("01/")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("01")))

        onView(exactView(testedView)).perform(typeText("2"))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("01/2")))
    }

    @Test
    @ConfigurationSingle
    fun shouldWorkBackspace() {
        onView(exactView(testedView)).perform(typeText("0122"))
        takeScreen()

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("01/2")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("01/")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("01")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("0")))

        onView(exactView(testedView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        takeScreen()
        onView(exactView(testedView)).check(matches(withText("")))
    }
}
