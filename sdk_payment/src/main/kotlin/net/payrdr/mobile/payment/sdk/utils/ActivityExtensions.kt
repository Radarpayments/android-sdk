package net.payrdr.mobile.payment.sdk.utils

import android.app.Activity
import android.content.Intent
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_ERROR
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData

/**
 * Terminates the [Activity] on which this method was called with passing as a result
 * [Activity] work with value [paymentData].
 *
 * @param paymentData - result of [Activity] work.
 */
fun Activity.finishWithResult(paymentData: PaymentData) {
    val resultIntent = Intent().apply {
        putExtra(INTENT_EXTRA_RESULT, paymentData)
    }
    setResult(Activity.RESULT_OK, resultIntent)
    finish()
}

/**
 * Terminates the [Activity] on which this method was called with passing as a result
 * [Activity] work with value [exception].
 *
 * @param exception - result of [Activity] work.
 */
fun Activity.finishWithError(exception: Exception) {
    val resultException = if (exception !is SDKException) {
        SDKException(
            cause = exception
        )
    } else {
        exception
    }
    val resultIntent = Intent().apply {
        putExtra(INTENT_EXTRA_ERROR, resultException)
    }
    setResult(Activity.RESULT_OK, resultIntent)
    finish()
}
