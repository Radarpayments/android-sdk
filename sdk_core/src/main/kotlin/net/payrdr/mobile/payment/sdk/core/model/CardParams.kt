package net.payrdr.mobile.payment.sdk.core.model

/**
 *  Card information.
 *
 *  @param mdOrder order number.
 *  @param pan card number.
 *  @param cvc secret crd code.
 *  @param expiryMMYY expiry date for card.
 *  @param cardHolder first and last name of cardholder.
 *  @param pubKey public key.
 * */

data class CardParams(
    val mdOrder: String = "",
    val pan: String,
    val cvc: String,
    val expiryMMYY: String,
    val cardHolder: String?,
    val pubKey: String
)
