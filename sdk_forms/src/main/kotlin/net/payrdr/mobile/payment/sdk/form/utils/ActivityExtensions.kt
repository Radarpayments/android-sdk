package net.payrdr.mobile.payment.sdk.form.utils

import android.app.Activity
import android.content.Intent
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.PaymentDataStatus

/**
 * Sets the default result of the [Activity] to [PaymentDataStatus.CANCELED].
 */
fun Activity.setupDefaultResult() {
    val resultIntent = Intent().apply {
        putExtra(
            Constants.INTENT_EXTRA_RESULT,
            CryptogramData(
                status = PaymentDataStatus.CANCELED,
                seToken = ""
            )
        )
    }
    setResult(Activity.RESULT_OK, resultIntent)
}

/**
 * Stops the [Activity] on which this method was called with passing as a result
 * job [Activity] values [cryptogram].
 *
 * @param cryptogram - result of [Activity]
 */
fun Activity.finishWithResult(cryptogram: CryptogramData) {
    val resultIntent = Intent().apply {
        putExtra(Constants.INTENT_EXTRA_RESULT, cryptogram)
    }
    setResult(Activity.RESULT_OK, resultIntent)
    finish()
}

/**
 * Stops the [Activity] on which this method was called with passing as a result
 * job [Activity] values [exception].
 *
 * @param exception - result of [Activity]
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
        putExtra(Constants.INTENT_EXTRA_ERROR, resultException)
    }
    setResult(Activity.RESULT_OK, resultIntent)
    finish()
}


/**
 * Stops the [Activity] by user cancellation payment.
 */
fun Activity.finishWithUserCancellation() {
    setResult(Activity.RESULT_CANCELED)
    finish()
}
