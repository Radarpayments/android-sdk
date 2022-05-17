package net.payrdr.mobile.payment.sdk.core

import android.Manifest
import android.app.Activity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule.grant
import net.payrdr.mobile.payment.sdk.testUtils.junit.ConfigurationRule
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestName

/**
 * Base class for creating UI tests. The class contains all the functions for
 * performing user interface tests.
 *
 * Granting the right to write files to the device's external storage.
 * Taking screenshots while running a test.
 * Environment configuration (theme and locale selection) of the test.
 *
 * @param activityClass the Activity class to run the test.
 * @param initialTouchMode true if the Activity should be put into "touch mode" on startup.
 * @param launchActivity true if the Activity needs to be started once.
 */
@LargeTest
open class CoreUITest<ACTIVITY : Activity>(
    activityClass: Class<ACTIVITY>,
    initialTouchMode: Boolean = true,
    launchActivity: Boolean = true
) {

    private val permissionRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val configurationRule = ConfigurationRule()

    private val testName = TestName()

    protected val activityTestRule =
        ActivityTestRule<ACTIVITY>(activityClass, initialTouchMode, launchActivity)

    protected fun getActivity(): ACTIVITY = activityTestRule.activity

    protected fun getString(resId: Int) = getActivity().resources.getString(resId)

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(configurationRule)
        .around(activityTestRule)
        .around(testName)
        .around(permissionRule)

    /**
     * Takes a snapshot of the current state of the Activity and attaches it to the test report.
     *
     * @param shotName name of the picture.
     * @param closeSoftKeyboard true to hide the soft keyboard before taking a snapshot.
     */
    fun takeScreen(shotName: String = "", closeSoftKeyboard: Boolean = true) {
        if (closeSoftKeyboard) {
            onView(isRoot()).perform(closeSoftKeyboard())
        }
        val methodName = testName.methodName
        val localeName = configurationRule.currentLocale.toString()
        val themeName = configurationRule.currentTheme.toString()
        val tag = listOf(methodName, shotName, localeName, themeName).filter {
            it.isNotBlank()
        }.joinToString(separator = "_", transform = { it.replace(tagRegex, "") })
    }

    companion object {

        /**
         * An expression to check for valid values ​​in the name of the screenshot file.
         */
        private val tagRegex = "[^a-zA-Z0-9_-]".toRegex()
    }
}
