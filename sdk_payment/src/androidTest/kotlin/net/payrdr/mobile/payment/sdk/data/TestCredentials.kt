package net.payrdr.mobile.payment.sdk.data

import androidx.test.platform.app.InstrumentationRegistry

/**
 * Gateway credentials for instrumented tests. They are not stored in the repository and are passed
 * as instrumentation arguments:
 *
 * ```
 * ./gradlew :sdk_payment:connectedAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.merchantLogin=<login> \
 *     -Pandroid.testInstrumentationRunnerArguments.merchantPassword=<password> \
 *     -Pandroid.testInstrumentationRunnerArguments.apiKey=<api key>
 * ```
 *
 * The same `android.testInstrumentationRunnerArguments.*` keys can be put into `~/.gradle/gradle.properties`.
 */
object TestCredentials {

    /** Merchant API login for `register.do`. */
    val merchantLogin: String
        get() = argument("merchantLogin")

    /** Merchant API password for `register.do`. */
    val merchantPassword: String
        get() = argument("merchantPassword")

    /** API key for API v2 sessions. */
    val apiKey: String
        get() = argument("apiKey")

    private fun argument(name: String): String =
        InstrumentationRegistry.getArguments().getString(name)?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "Missing instrumentation argument '$name'. " +
                    "Pass it with -Pandroid.testInstrumentationRunnerArguments.$name=<value>"
            )
}
