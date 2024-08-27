package net.payrdr.mobile.payment.sdk.payment.model

/**
 *  An object containing data for API requests.
 *
 *  @param paymentToken cryptogram.
 *  @param mdOrder order number.
 *  @param holder first and last cardholder name.
 *  @param saveCard should saving a card.
 *  @param additionalPayerData list of additional information about payer.
 *  @param email payer email.
 *  @param mobilePhone payer mobile phone.
 */
data class ProcessFormRequest(
    val paymentToken: String,
    val mdOrder: String,
    val holder: String,
    val saveCard: Boolean,
    val additionalPayerData: Map<String, String>,
    val email: String?,
    val mobilePhone: String?
)
