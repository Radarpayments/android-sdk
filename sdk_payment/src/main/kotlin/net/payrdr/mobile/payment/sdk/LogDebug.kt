package net.payrdr.mobile.payment.sdk

import android.util.Log

/**
 * Restricted logging methods used in the Payment SDK.
 */
object LogDebug {

    /**
     * Print logs if build is in debug mode.
     *
     * @param message the output line to the log.
     */
    fun logIfDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("PAYRDRSDK", message)
        }
    }
}
