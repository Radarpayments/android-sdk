package net.payrdr.mobile.payment.sdk.payment.model

/**
 *  An object containing data for API requests.
 *
 *  @param seToken cryptogram.
 *  @param mdOrder order number.
 *  @param holder first and last cardholder name.
 *  @param saveCard should saving a card.
 */
data class ProcessFormRequest(
    val seToken: String,
    val mdOrder: String,
    val holder: String,
    val saveCard: Boolean,
)
