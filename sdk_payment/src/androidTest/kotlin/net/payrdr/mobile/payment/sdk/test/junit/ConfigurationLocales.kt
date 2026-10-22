package net.payrdr.mobile.payment.sdk.test.junit

/**
 * Annotation to run the test in several locales instead of the default English one.
 *
 * Usage example:
 *
 *      @Test
 *      @ConfigurationLocales(["fr", "es"])
 *      fun test() {
 *      }
 *
 * The test will be run twice, first in the "fr" locale, then in "es".
 * Without arguments the test runs in every locale from
 * [net.payrdr.mobile.payment.sdk.ui.helper.Locales.availableLocales].
 *
 * @param locales an array of locales in which the test should run.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ConfigurationLocales(val locales: Array<String> = [])
