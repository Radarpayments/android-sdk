package net.payrdr.mobile.payment.sdk.core.model

/**
 *  Information about the binding card for payment with old method.
 *
 *  @param mdOrder order number.
 *  @param bindingID number of binding card.
 *  @param cvc secret code for card.
 *  @param pubKey public key.
 */

data class BindingParams(
    val mdOrder: String = "",
    val bindingID: String,
    override val cvc: String?,
    override val pubKey: String
) : PaymentCardParams.StoredCardPaymentParams(cvc = cvc, pubKey = pubKey)
