package net.payrdr.mobile.payment.sdk.form.component

import net.payrdr.mobile.payment.sdk.core.model.CardInfo

/**
 * Interface for the processor which create a cryptogram based on the transferred payment data.
 */
interface CryptogramProcessor {

    /**
     * Creates a cryptogram ready for payment.
     *
     * @param order payment identifier.
     * @param timestamp payment data.
     * @param uuid unique identifier.
     * @param cardInfo card data for debiting funds.
     * @return cryptogram for the transferred payment data.
     */
    suspend fun create(
        order: String = "",
        timestamp: Long,
        uuid: String,
        cardInfo: CardInfo
    ): String

    /**
     * Creates a cryptogram ready for payment.
     *
     * @param googlePayToken token received from Google Pay.
     * @return cryptogram for the transferred payment data.
     */
    suspend fun create(
        googlePayToken: String
    ): String
}
