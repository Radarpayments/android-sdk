package net.payrdr.mobile.payment.sdk.test.junit

import net.payrdr.mobile.payment.sdk.form.model.Theme
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales
import net.payrdr.mobile.payment.sdk.form.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.form.ui.helper.ThemeSetting
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale

/**
 * JUnit rule for running tests with the specified environment settings.
 *
 * The [defaultLocales] and [defaultThemes] lists are used by default for all tests that are run.
 *
 * If you need to redefine the parameters individually for the test, you can use the annotations:
 *
 * [ConfigurationLocales] - to override the list of locales.
 * [ConfigurationThemes] - to override the list of topics.
 *
 * @param defaultLocales a list of locales in which tests should run by default.
 * @param defaultThemes list of topics in which tests should run by default.
 */
class ConfigurationRule(
    private val defaultLocales: List<Locale> = Locales.availableLocales(),
    private val defaultThemes: List<Theme> = listOf(Theme.LIGHT, Theme.DARK)
) : TestRule {

    /**
     * The current locale in which the test is running.
     */
    lateinit var currentLocale: Locale
        private set

    /**
     * The current topic in which the test is running.
     */
    lateinit var currentTheme: Theme
        private set

    override fun apply(
        base: Statement,
        description: Description
    ): Statement {
        val testLocales = defaultLocales.toMutableList()
        val testThemes = defaultThemes.toMutableList()
        description.annotations.forEach { annotation ->
            when (annotation) {
                is ConfigurationLocales -> {
                    testLocales.clear()
                    testLocales.addAll(
                        annotation.locales.map {
                            Locale.Builder().setLanguage(it).build()
                        }
                    )
                }
                is ConfigurationThemes -> {
                    testThemes.clear()
                    testThemes.addAll(annotation.themes)
                }
                is ConfigurationSingle -> {
                    testLocales.clear()
                    testThemes.clear()
                    testLocales.add(Locales.english())
                    testThemes.add(Theme.DARK)
                }
            }
        }
        check(testLocales.isNotEmpty()) { "Test locales list should not be empty" }
        check(testThemes.isNotEmpty()) { "Test themes list should not be empty" }
        return ConfigurationRuleStatement(
            base = base,
            locales = testLocales,
            themes = testThemes
        )
    }

    /**
     * Runs the specified [base] test expression, iterating over all possible combinations
     * of the list of available locales and the list of available themes to test.
     *
     * @param base test expression.
     * @param locales the list of locales in which the test will be run.
     * @param themes list of topics in which the test will be run.
     */
    private inner class ConfigurationRuleStatement(
        private val base: Statement,
        private val locales: List<Locale>,
        private val themes: List<Theme>
    ) : Statement() {

        @Throws(Throwable::class)
        override fun evaluate() {
            themes.forEach { theme ->
                currentTheme = theme
                ThemeSetting.setTheme(theme)
                locales.forEach { locale ->
                    LocalizationSetting.setLanguage(locale)
                    currentLocale = locale
                    base.evaluate()
                }
            }
        }
    }
}
