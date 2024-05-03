package net.payrdr.mobile.payment.sdk.payment

import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig

/**
 *  Interface describing activity delegates methods.
 */
interface ActivityDelegate {

    /**
     * Finish activity and return result of the job.
     *
     * @param paymentData result of payment completion.
     */
    fun finishActivityWithResult(paymentData: PaymentResult)

    /**
     * Getting a configuration object for the Payment SDK.
     *
     * @return configuration object.
     */
    fun getPaymentConfig(): SDKPaymentConfig

}
