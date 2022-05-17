package net.payrdr.mobile.payment.sdk.test.junit

import net.payrdr.mobile.payment.sdk.form.model.Theme

/**
 * An annotation to run a test with a specified enumeration of topics to test.
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
 *
 * @param themes an array of topics against which the test should run.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ConfigurationThemes(val themes: Array<Theme> = [])
