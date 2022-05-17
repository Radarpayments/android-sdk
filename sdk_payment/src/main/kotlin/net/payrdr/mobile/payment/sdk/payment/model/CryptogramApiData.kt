package net.payrdr.mobile.payment.sdk.payment.model

/**
 *  An object containing data for API requests.
 *
 *  @param cryptogram cryptogram.
 *  @param mdOrder order number.
 *  @param holder first and last cardholder name.
 *  @param saveCard should saving a card.
 */
data class CryptogramApiData(
    val cryptogram: String,
    val mdOrder: String,
    val holder: String,
    val saveCard: Boolean = false
)
