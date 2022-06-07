package net.payrdr.mobile.payment.sdk.core.model

/**
 *  Information about binding card.
 *
 *  @param mdOrder order number.
 *  @param bindingID number of binding card.
 *  @param cvc secret code for card.
 *  @param pubKey public key.
 */

data class BindingParams(
    val mdOrder: String = "",
    val bindingID: String,
    val cvc: String?,
    val pubKey: String
)
