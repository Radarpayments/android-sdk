package net.payrdr.mobile.payment.sdk.ui.widget

import android.content.Context
import android.view.KeyEvent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.TypeTextAction
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
import net.payrdr.mobile.payment.sdk.form.ui.widget.CardNumberEditText
import net.payrdr.mobile.payment.sdk.form.utils.onDisplayError
import net.payrdr.mobile.payment.sdk.test.core.CoreUIViewTest
import net.payrdr.mobile.payment.sdk.test.espresso.ExactViewMatcher.Companion.exactView
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationSingle
import org.junit.Test

@SmallTest
class CardNumberEditTextTest : CoreUIViewTest<CardNumberEditText>() {

    override fun prepareView(context: Context): CardNumberEditText {
        return CardNumberEditText(context).apply {
            showError = true
            hint = getString(R.string.payrdr_card_number)
        }
    }

    override fun wrapView(context: Context, view: CardNumberEditText): View {
        return BaseTextInputLayout(context).apply {
            addView(view)
            view onDisplayError { this.error = it }
        }
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeLatin")
    fun shouldNotAllowTypeLatin() {
        onView(exactView(testedView)).perform(typeText("abcDess"))

        onView(exactView(testedView)).check(matches(withText("")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeCyrillic")
    fun shouldNotAllowTypeCyrillic() {
        onView(exactView(testedView)).perform(replaceText("Ворота"))
        onView(exactView(testedView)).perform(typeText("1234"))

        onView(exactView(testedView)).check(matches(withText("1234")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldDisplayCorrectFormat")
    fun shouldDisplayCorrectFormat() {
        onView(exactView(testedView)).perform(typeText("1234"))
        allureScreenshot(name = "shouldDisplayCorrectFormat_1", quality = 1)
        onView(exactView(testedView)).check(matches(withText("1234")))

        onView(exactView(testedView)).perform(typeText("5"))
        allureScreenshot(name = "shouldDisplayCorrectFormat_2", quality = 1)
        onView(exactView(testedView)).check(matches(withText("1234 5")))

        onView(exactView(testedView)).perform(typeText("6"))
        allureScreenshot(name = "shouldDisplayCorrectFormat_3", quality = 1)
        onView(exactView(testedView)).check(matches(withText("1234 56")))

        onView(exactView(testedView)).perform(typeText("7"))
        allureScreenshot(name = "shouldDisplayCorrectFormat_4", quality = 1)
        onView(exactView(testedView)).check(matches(withText("1234 567")))

        onView(exactView(testedView)).perform(typeText("8"))
        allureScreenshot(name = "shouldDisplayCorrectFormat_5", quality = 1)
        onView(exactView(testedView)).check(matches(withText("1234 5678")))

        onView(exactView(testedView)).perform(typeText("9"))
        allureScreenshot(name = "shouldDisplayCorrectFormat_6", quality = 1)
        onView(exactView(testedView)).check(matches(withText("1234 5678 9")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldAcceptMinNumberLength")
    fun shouldAcceptMinNumberLength() {
        onView(exactView(testedView)).perform(typeText("5391119268214792"))
        allureScreenshot(name = "shouldAcceptMinNumberLength_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("5391 1192 6821 4792")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldAcceptMaxNumberLength")
    fun shouldAcceptMaxNumberLength() {
        onView(exactView(testedView)).perform(typeText("3536360489308537405"))
        allureScreenshot(name = "shouldAcceptMaxNumberLength_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("3536 3604 8930 8537405")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeMoreThenMaxLength")
    fun shouldNotAllowTypeMoreThenMaxLength() {
        onView(exactView(testedView)).perform(typeText("35363604893085374051"))
        allureScreenshot(name = "shouldNotAllowTypeMoreThenMaxLength_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("3536 3604 8930 8537405")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldWorkBackspace")
    fun shouldWorkBackspace() {
        onView(exactView(testedView)).perform(typeText("3536360489308537405"))
        allureScreenshot(name = "shouldWorkBackspace_1", quality = 1)
        onView(exactView(testedView)).check(matches(withText("3536 3604 8930 8537405")))

        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_2", quality = 1)
        onView(exactView(testedView)).check(matches(withText("3536 3604 8930 853740")))

        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_3", quality = 1)
        onView(exactView(testedView)).check(matches(withText("3536 3604 8930 85374")))

        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_4", quality = 1)
        onView(exactView(testedView)).check(matches(withText("3536 3604 8930 8537")))

        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkBackspace_5", quality = 1)
        onView(exactView(testedView)).check(matches(withText("3536 3604 8930")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldWorkEdit")
    fun shouldWorkEdit() {
        onView(exactView(testedView)).perform(typeText("35363604"))
        allureScreenshot(name = "shouldWorkEdit_1", quality = 1)
        onView(exactView(testedView)).check(matches(withText("3536 3604")))

        activityTestRule.runOnUiThread {
            testedView.setSelection(3)
        }
        allureScreenshot(name = "shouldWorkEdit_2", quality = 1)

        onView(exactView(testedView)).perform(pressKey(KeyEvent.KEYCODE_DEL))
        allureScreenshot(name = "shouldWorkEdit_3", quality = 1)

        onView(exactView(testedView)).check(matches(withText("3563 604")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldWorkInsert")
    fun shouldWorkInsert() {
        onView(exactView(testedView)).perform(typeText("35363604"))
        allureScreenshot(name = "shouldWorkInsert_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("3536 3604")))

        onView(exactView(testedView))
            .perform(
                GeneralClickAction(
                    Tap.SINGLE,
                    GeneralLocation.CENTER_LEFT,
                    Press.FINGER,
                    0,
                    0,
                    null
                ),
                TypeTextAction("9", false),
                TypeTextAction("7", false)
            )
        allureScreenshot(name = "shouldWorkInsert_2", quality = 1)

        onView(exactView(testedView)).check(matches(withText("9735 3636 04")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldCheckNumberByLunaAlgorithm")
    fun shouldCheckNumberByLunaAlgorithm() {
        onView(exactView(testedView)).perform(typeText("4485873525931601"))
        allureScreenshot(name = "shouldCheckNumberByLunaAlgorithm_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("4485 8735 2593 1601")))
    }
}
