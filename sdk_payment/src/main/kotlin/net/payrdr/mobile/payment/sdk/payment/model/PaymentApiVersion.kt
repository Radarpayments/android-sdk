package net.payrdr.mobile.payment.sdk.payment.model

/**
 * Version of API using for payment. Depends on [CheckoutConfig].
 *
 */

enum class PaymentApiVersion {

    /**
     *  When checkout with MdOrder.
     */
    V1,

    /**
     * When checkout with sessionId.
     */
    V2
}
