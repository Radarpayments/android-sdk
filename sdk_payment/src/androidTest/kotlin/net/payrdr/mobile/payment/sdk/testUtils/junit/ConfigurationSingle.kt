package net.payrdr.mobile.payment.sdk.testUtils.junit

/**
 * Annotation to run the test in one locale and in one theme.
 *
 * Used to run tests that don't need to iterate over themes and locales.
 *
 * Usage example:
 *
 *      @Test
 *      @ConfigurationSingle
 *      fun test() {
 *      }
 *
 * The test will run in the "en" locale of the "Theme.LIGHT" light theme.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ConfigurationSingle
