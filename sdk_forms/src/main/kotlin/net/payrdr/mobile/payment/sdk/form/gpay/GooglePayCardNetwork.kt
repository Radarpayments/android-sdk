package net.payrdr.mobile.payment.sdk.form.gpay

/**
 * Possible ways to receive payment by cards.
 *
 * @param value the value passed to Google Pay.
 */
enum class GooglePayCardNetwork(val value: String) {
    AMEX("AMEX"),
    DISCOVER("DISCOVER"),
    INTERAC("INTERAC"),
    JCB("JCB"),
    MASTERCARD("MASTERCARD"),
    VISA("VISA")
}
