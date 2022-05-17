package net.payrdr.mobile.payment.sdk.test.junit

/**
 * Annotation to run the test with the specified enumeration of locales to test.
 *
 * Usage example:
 *
 *      @Test
 *      @ConfigurationLocales(["fr", "es"])
 *      fun test() {
 *      }
 *
 * The test will be run twice, first in the "fr" locale, then in "es".
 *
 * @param locales an array of locales in which the test should run.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ConfigurationLocales(val locales: Array<String> = [])
