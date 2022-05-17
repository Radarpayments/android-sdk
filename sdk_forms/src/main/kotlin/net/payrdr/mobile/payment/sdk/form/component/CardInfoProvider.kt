package net.payrdr.mobile.payment.sdk.form.component

/**
 * Interface for obtaining information about the card by its number.
 */
interface CardInfoProvider {

    /**
     * Method of obtaining information about a card by its number.
     *
     * @param bin card number or first 6-8 digits of the number.
     */
    suspend fun resolve(bin: String): CardInfo
}
