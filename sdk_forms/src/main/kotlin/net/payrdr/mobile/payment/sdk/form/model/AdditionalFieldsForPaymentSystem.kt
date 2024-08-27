package net.payrdr.mobile.payment.sdk.form.model

/**
 * Container for lists of additional fields for VISA and MASTERCARD payment system.
 * @param visaFields the list of additional fields about payer to fill when pay by VISA.
 * @param mastercardFields the list of additional fields about payer to fill when pay by MASTERCARD.
 */
data class AdditionalFieldsForPaymentSystem(
    val visaFields: List<AdditionalField>,
    val mastercardFields: List<AdditionalField>
)
