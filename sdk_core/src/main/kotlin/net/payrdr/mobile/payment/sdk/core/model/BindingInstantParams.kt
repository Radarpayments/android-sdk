package net.payrdr.mobile.payment.sdk.core.model

/**
 * Information about binding card for instant payment with old method.
 *
 *  @param bindingID number of binding card.
 *  @param cvc secret code for card.
 *  @param pubKey public key.
 */
data class BindingInstantParams(
    val bindingID: String,
    override val cvc: String?,
    override val pubKey: String
) : PaymentCardParams.StoredCardPaymentParams(cvc = cvc, pubKey = pubKey)
