package net.payrdr.mobile.payment.sdk.form.model

/**
 * Possible withdrawal options for saving the card after payment.
 */
enum class CardSaveOptions {

    /**
     * Save card option hidden.
     */
    HIDE,

    /**
     * Card save option, default value: Yes.
     */
    YES_BY_DEFAULT,

    /**
     * Card save option, default value: No.
     */
    NO_BY_DEFAULT
}
