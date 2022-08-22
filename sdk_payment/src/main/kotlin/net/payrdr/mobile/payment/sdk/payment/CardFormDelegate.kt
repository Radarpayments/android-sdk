package net.payrdr.mobile.payment.sdk.payment

import net.payrdr.mobile.payment.sdk.api.entity.BindingItem
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig

/**
 * Interface describing the operation of the card data entry form.
 */
interface CardFormDelegate {

    /**
     * Method for starting a form with binding cards.
     *
     * @param mdOrder order number.
     * @param bindingEnabled is card saving allowed (check mark pressed).
     * @param bindingCards list of binding cards.
     * @param cvcNotRequired cvc not required.
     * @param bindingDeactivationEnabled possible to delete cards.
     * @param googlePayConfig configuration for google pay payment.
     */
    @Suppress("LongParameterList")
    fun openBottomSheet(
        mdOrder: String,
        bindingEnabled: Boolean,
        bindingCards: List<BindingItem>,
        cvcNotRequired: Boolean,
        bindingDeactivationEnabled: Boolean,
        googlePayConfig: GooglePayPaymentConfig
    )
}
