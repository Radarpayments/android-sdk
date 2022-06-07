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
import net.payrdr.mobile.payment.sdk.form.ui.widget.CardCodeEditText
import net.payrdr.mobile.payment.sdk.form.utils.onDisplayError
import net.payrdr.mobile.payment.sdk.test.core.CoreUIViewTest
import net.payrdr.mobile.payment.sdk.test.espresso.ExactViewMatcher.Companion.exactView
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationSingle
import org.junit.Test

@SmallTest
class CardCodeEditTextTest : CoreUIViewTest<CardCodeEditText>() {

    override fun prepareView(context: Context): CardCodeEditText {
        return CardCodeEditText(context).apply {
            showError = true
            hint = getString(R.string.payrdr_code)
        }
    }

    override fun wrapView(context: Context, view: CardCodeEditText): View {
        return BaseTextInputLayout(context).apply {
            addView(view)
            view onDisplayError { this.error = it }
        }
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeLatin")
    fun shouldNotAllowTypeLatin() {
        onView(exactView(testedView)).perform(typeText("abcDe"))
        allureScreenshot(name = "shouldNotAllowTypeLatin_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeCyrillic")
    fun shouldNotAllowTypeCyrillic() {
        onView(exactView(testedView)).perform(replaceText("абГд"))
        allureScreenshot(name = "shouldNotAllowTypeCyrillic_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldNotAllowTypeMoreThenMaxLength")
    fun shouldNotAllowTypeMoreThenMaxLength() {
        onView(exactView(testedView)).perform(typeText("1225"))
        allureScreenshot(name = "shouldNotAllowTypeMoreThenMaxLength_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("122")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldMaskInput")
    fun shouldMaskInput() {
        onView(exactView(testedView)).perform(typeText("012"))
        allureScreenshot(name = "shouldMaskInput_1", quality = 1)

        onView(exactView(testedView)).check(matches(withText("012")))
    }

    @Test
    @ConfigurationSingle
    @Description("shouldWorkBackspace")
    fun shouldWorkBackspace() {
        onView(exactView(testedView)).perform(typeText("012"))
        allureScreenshot(name = "shouldWorkBackspace_1", quality = 1)
        onView(exactView(testedView))
            .perform(pressKey(KeyEvent.KEYCODE_DEL)).apply { takeScreen() }
            .perform(pressKey(KeyEvent.KEYCODE_DEL)).apply { takeScreen() }
            .perform(pressKey(KeyEvent.KEYCODE_DEL)).apply { takeScreen() }

        onView(exactView(testedView)).check(matches(withText("")))
    }
}
