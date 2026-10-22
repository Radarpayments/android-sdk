package net.payrdr.mobile.payment.sample.kotlin.helpers

import android.content.Context
import android.util.Log
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sdk.SDKCanceledByUserException
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult

/**
 * Human-readable description of the payment result for the sample screens.
 *
 * Shows how to tell the outcomes apart: a user cancellation comes as [SDKCanceledByUserException]
 * and should not be treated as a failed payment.
 */
fun PaymentResult.toReadableText(context: Context): String =
    if (exception is SDKCanceledByUserException) {
        context.getString(R.string.result_cancelled)
    } else {
        // The exception is shown for a successful result too: e.g. an already paid order
        // comes as isSuccess = true with SDKAlreadyPaymentException.
        val notAvailable = context.getString(R.string.result_not_available)
        listOf(
            context.getString(if (isSuccess) R.string.result_success else R.string.result_failed),
            context.getString(R.string.result_payment_id, sessionId),
            context.getString(R.string.result_exception, exception?.javaClass?.simpleName ?: notAvailable),
            context.getString(
                R.string.result_exception_message,
                exception?.message?.takeIf { it.isNotBlank() } ?: notAvailable
            ),
        ).joinToString("\n\n")
    }

/**
 * Writes the raw payment result to logcat for debugging.
 */
fun PaymentResult.logRaw() {
    Log.d("LOG_TAG", "PaymentData($sessionId, $isSuccess $exception)")
}
