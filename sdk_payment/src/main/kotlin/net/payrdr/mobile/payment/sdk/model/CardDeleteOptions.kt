package net.payrdr.mobile.payment.sdk.model

/**
 * Possible options for removing a card in the process of creating a cryptogram.
 */
enum class CardDeleteOptions {

    /**
     * Remove card option, default value: Yes.
     */
    YES_DELETE,

    /**
     * Remove card option, default value: None.
     */
    NO_DELETE
}
