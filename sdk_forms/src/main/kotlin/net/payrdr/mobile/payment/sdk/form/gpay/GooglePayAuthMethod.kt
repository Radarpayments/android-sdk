package net.payrdr.mobile.payment.sdk.form.gpay

/**
 * Possible authorization methods.
 *
 * @param value the value passed to Google Pay.
 */
enum class GooglePayAuthMethod(val value: String) {
    PAN_ONLY("PAN_ONLY"),
    CRYPTOGRAM_3DS("CRYPTOGRAM_3DS")
}
