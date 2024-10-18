package net.payrdr.mobile.payment.sdk.payment

/**
 *  Manager interface for managing the payment process.
 */
interface PaymentManager {

    /**
     * Start the payment process for cards.
     *
     * @param order order number.
     */
    fun checkout(order: String, gPayClicked: Boolean)

}
