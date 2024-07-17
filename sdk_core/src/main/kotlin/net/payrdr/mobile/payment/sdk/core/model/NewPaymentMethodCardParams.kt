package net.payrdr.mobile.payment.sdk.core.model

/**
 *  Information about card for payment with new method.
 *
 *  @param pan card number.
 *  @param cvc secret crd code.
 *  @param expiryMMYY expiry date for card.
 *  @param cardHolder first and last name of cardholder.
 *  @param pubKey public key.
 * */

data class NewPaymentMethodCardParams(
    override val pan: String,
    override val cvc: String,
    override val expiryMMYY: String,
    override val cardHolder: String?,
    override val pubKey: String
) : PaymentCardParams.NewCardPaymentParams(
    pan = pan,
    cvc = cvc,
    expiryMMYY = expiryMMYY,
    cardHolder = cardHolder,
    pubKey = pubKey
)
