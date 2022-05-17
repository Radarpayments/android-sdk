package net.payrdr.mobile.payment.sdk.form.gpay

/**
 * Possible variants of the final price.
 *
 * @param value the value passed to Google Pay.
 */
enum class GooglePayTotalPriceStatus(val value: String) {
    FINAL("FINAL")
}
