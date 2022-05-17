package net.payrdr.mobile.payment.sdk.form.gpay

import android.content.Context
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import net.payrdr.mobile.payment.sdk.form.utils.deviceHasGooglePlayServices
import org.json.JSONObject

/**
 * Helper class for generating a request to process a payment via Google Pay.
 */
@Suppress("TooManyFunctions")
object GooglePayUtils {

    /**
     * Method of creating a customer for making a payment via Google Pay.
     *
     * @param context application context.
     * @param environment environment in which the client will work.
     * @return PaymentsClient .
     */
    fun createPaymentsClient(
        context: Context,
        environment: Int
    ): PaymentsClient = Wallet.getPaymentsClient(
        context,
        Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()
    )

    /**
     * Method for checking the ability to make a payment via Google Pay.
     *
     * @param context application context.
     * @param isReadyToPayJson json with a description of the payment.
     * @param paymentsClient client to make a payment.
     * @param callback a listener to get the result of the check.
     */
    fun possiblyShowGooglePayButton(
        context: Context,
        isReadyToPayJson: JSONObject,
        paymentsClient: PaymentsClient,
        callback: GooglePayCheckCallback
    ) {
        val servicesAvailable = deviceHasGooglePlayServices(context)
        if (servicesAvailable) {
            val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
            paymentsClient.isReadyToPay(request).addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    callback.onReadyToRequest()
                } else {
                    callback.onNotReadyToRequest()
                }
            }
        } else {
            callback.onNoGooglePlayServices()
        }
    }

    /**
     * Listener interface for receiving the result of checking the ability to make a payment
     * via Google Pay on the user's device.
     */
    interface GooglePayCheckCallback {

        /**
         * Called in the absence of Google Play services on the user's device.
         */
        fun onNoGooglePlayServices()

        /**
         * Called when there is no way to make a payment via Google Pay.
         */
        fun onNotReadyToRequest()

        /**
         * Called when it is possible to make a payment via Google Pay.
         */
        fun onReadyToRequest()
    }
}
