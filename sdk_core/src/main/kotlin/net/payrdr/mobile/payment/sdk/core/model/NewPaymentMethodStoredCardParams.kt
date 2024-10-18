package net.payrdr.mobile.payment.sdk.core.model

/**
 *  Information about stored card for payment with new method.
 *
 *  @param storedPaymentId id for stored payment.
 *  @param cvc secret code for card.
 *  @param pubKey public key.
 * */

data class NewPaymentMethodStoredCardParams(
    val storedPaymentId: String,
    override val cvc: String?,
    override val pubKey: String
) : PaymentCardParams.StoredCardPaymentParams(
    cvc = cvc,
    pubKey = pubKey
)
