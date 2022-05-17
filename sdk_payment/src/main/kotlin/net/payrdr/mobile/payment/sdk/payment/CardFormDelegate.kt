package net.payrdr.mobile.payment.sdk.payment

import net.payrdr.mobile.payment.sdk.api.entity.BindingItem

/**
 * Interface describing the operation of the card data entry form.
 */
interface CardFormDelegate {

    /**
     * Method for starting a form with a new card.
     *
     * @param mdOrder order number.
     * @param bindingEnabled is it possible to display a check mark to save a card.
     */
    fun openNewCardForm(mdOrder: String, bindingEnabled: Boolean)

    /**
     * Method for starting a form with binding cards.
     *
     * @param mdOrder order number.
     * @param bindingEnabled is card saving allowed (check mark pressed).
     * @param bindingCards list of binding cards.
     * @param cvcNotRequired cvc not required.
     */
    fun openBindingCardForm(
        mdOrder: String,
        bindingEnabled: Boolean,
        bindingCards: List<BindingItem>,
        cvcNotRequired: Boolean,
        bindingDeactivationEnabled: Boolean
    )
}
