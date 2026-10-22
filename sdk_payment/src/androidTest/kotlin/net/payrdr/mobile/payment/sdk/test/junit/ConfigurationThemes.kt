package net.payrdr.mobile.payment.sdk.test.junit

import net.payrdr.mobile.payment.sdk.model.Theme

/**
 * Annotation to run the test in several themes instead of the default light one.
 *
 * Usage example:
 *
 *      @Test
 *      @ConfigurationThemes([Theme.DARK, Theme.LIGHT])
 *      fun test() {
 *      }
 *
 * The test will be run twice, first in the dark theme "Theme.DARK",
 * then in the light theme "Theme.LIGHT".
 * Without arguments the test runs in both the light and the dark theme.
 *
 * @param themes an array of themes in which the test should run.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ConfigurationThemes(val themes: Array<Theme> = [])
