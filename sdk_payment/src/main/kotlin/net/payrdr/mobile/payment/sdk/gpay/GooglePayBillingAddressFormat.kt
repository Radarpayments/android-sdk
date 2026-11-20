package net.payrdr.mobile.payment.sdk.gpay

/**
 * Billing address formats.
 *
 * @param value the value passed to Google Pay.
 */
enum class GooglePayBillingAddressFormat(val value: String) {
    MIN("MIN"),
    FULL("FULL")
}
