package net.payrdr.mobile.payment.sdk.payment.model

/**
 * Configuration for constructing order number.
 */

sealed class CheckoutConfig {

    /**
     * Use when have exact order number.
     *
     * @param value order number.
     */
    data class MdOrder(val value: String) : CheckoutConfig()

    /**
     * Use when have session id.
     *
     * @param value session id to parse order number.
     */

    data class SessionId(val value: String) : CheckoutConfig()
}
