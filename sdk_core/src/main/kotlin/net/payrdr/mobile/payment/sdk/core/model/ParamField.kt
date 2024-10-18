package net.payrdr.mobile.payment.sdk.core.model

/**
 * Possible error locations.
 */
enum class ParamField {

    /**
     * Unknown error.
     */
    UNKNOWN,

    /**
     * Card number error.
     */
    PAN,

    /**
     * CVC error.
     */
    CVC,

    /**
     * Expiry date error.
     */
    EXPIRY,

    /**
     * Cardholder error.
     */
    CARDHOLDER,

    /**
     * Binding number error.
     */
    BINDING_ID,

    /**
     * Number order error.
     */
    MD_ORDER,

    /**
     * Public key error.
     */
    PUB_KEY,

    /**
     * Stored payment id error.
     */
    STORED_PAYMENT_ID
}
