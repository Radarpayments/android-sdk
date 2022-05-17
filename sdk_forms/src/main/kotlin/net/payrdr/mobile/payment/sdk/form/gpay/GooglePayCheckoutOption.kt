package net.payrdr.mobile.payment.sdk.form.gpay

/**
 * Possible payment options.
 *
 * @param value the value passed to Google Pay.
 */
enum class GooglePayCheckoutOption(val value: String) {
    COMPLETE_IMMEDIATE_PURCHASE("COMPLETE_IMMEDIATE_PURCHASE")
}
