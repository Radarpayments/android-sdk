package net.payrdr.mobile.payment.sdk.ui.widget

import android.content.Context
import android.view.KeyEvent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputLayout
import net.payrdr.mobile.payment.sdk.form.ui.widget.CardHolderEditText
import net.payrdr.mobile.payment.sdk.test.core.CoreUIViewTest
import net.payrdr.mobile.payment.sdk.test.espresso.ExactViewMatcher.Companion.exactView
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationSingle
import org.junit.Test

@SmallTest
class CardHolderEditTextTest : CoreUIViewTest<CardHolderEditText>() {

    override fun prepareView(context: Context): CardHolderEditText {
        return CardHolderEditText(context).apply {
            showError = true
            hint = getString(R.string.payrdr_card_holder_placeholder)
        }
    }

    override fun wrapView(context: Context, view: CardHolderEditText): View {
        return BaseTextInputLayout(context).apply {
            addView(view)
        }
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeDigitsAtTheStart")
    fun shouldNotAllowTypeDigitsAtTheStart() {
        onView(exactView(testedView)).perform(typeText("123abcDe"))
        allureScreenshot(name = "shouldNotAllowTypeDigitsAtTheStart_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("ABCDE")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeDigitsAtTheMiddle")
    fun shouldNotAllowTypeDigitsAtTheMiddle() {
        onView(exactView(testedView)).perform(typeText("Ho23me"))
        allureScreenshot(name = "shouldNotAllowTypeDigitsAtTheMiddle_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("HOME")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeDigitsAtTheEnd")
    fun shouldNotAllowTypeDigitsAtTheEnd() {
        onView(exactView(testedView)).perform(typeText("Home23"))
        allureScreenshot(name = "shouldNotAllowTypeDigitsAtTheEnd_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("HOME")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeCyrillicAtTheEnd")
    fun shouldNotAllowTypeCyrillicAtTheEnd() {
        onView(exactView(testedView)).perform(typeText("JUMP"))
        allureScreenshot(name = "shouldNotAllowTypeCyrillicAtTheEnd_1", quality = 1)
        onView(exactView(testedView)).perform(replaceText("вода"))
        allureScreenshot(name = "shouldNotAllowTypeCyrillicAtTheEnd_2", quality = 1)

        onView(exactView(testedView)).check(matches(withText("")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldAllowTypeLatinWithSpace")
    fun shouldAllowTypeLatinWithSpace() {
        onView(exactView(testedView)).perform(typeText("John Doe"))
        allureScreenshot(name = "shouldAllowTypeLatinWithSpace_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("JOHN DOE")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldWorkBackspace")
    fun shouldWorkBackspace() {
        onView(exactView(testedView)).perform(typeText("John Doe"))
        allureScreenshot(name = "shouldWorkBackspace_1", quality = 1)
        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_2", quality = 1)

        onView(exactView(testedView)).check(matches(withText("JOHN D")))
    }
}
