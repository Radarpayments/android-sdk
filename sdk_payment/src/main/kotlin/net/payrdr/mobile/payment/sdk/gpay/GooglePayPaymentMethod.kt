package net.payrdr.mobile.payment.sdk.gpay

/**
 * Possible payment options.
 *
 * @param value the value passed to Google Pay.
 */
enum class GooglePayPaymentMethod(val value: String) {
    CARD("CARD")
}
