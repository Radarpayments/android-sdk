package net.payrdr.mobile.payment.sdk.payment.model

import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig

/**
 * Interface describing the operation of the Google Pay method.
 */
interface GPayDelegate {

    /**
     * Method for starting a form with a google pay.
     *
     * @param config the configuration file for Google Pay.
     */
    fun openGPayForm(config: GooglePayPaymentConfig)
}
