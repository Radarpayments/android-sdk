package net.payrdr.mobile.payment.sdk.payment

import net.payrdr.mobile.payment.sdk.payment.model.PaymentApiVersion

/**
 *  Manager interface for managing the payment process.
 */
interface PaymentManager {

    /**
     * Start the payment process for cards.
     *
     * @param order order number.
     * @param versionApi version of current API for payment.
     */
    fun checkout(order: String, gPayClicked: Boolean, versionApi: PaymentApiVersion)

}
