package net.payrdr.mobile.payment.sdk

/**
 * Handles action deleting a saved card.
 */
interface DeleteCardHandler {

    /**
     * Unbind card on server.
     *
     * @param bindingId identifier of binding card.
     * @return true if success, otherwise false
     */
    fun deleteCard(bindingId: String)
}
