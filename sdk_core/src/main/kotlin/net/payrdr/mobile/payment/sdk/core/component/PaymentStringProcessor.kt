package net.payrdr.mobile.payment.sdk.core.component

import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom

/**
 * Payment data generation processor interface by template .
 */
interface PaymentStringProcessor {

    /**
     * Forms a line with billing information.
     *
     * @param order order identifier.
     * @param timestamp payment data.
     * @param uuid unique identifier.
     * @param cardInfo card data for withdraw money.
     * @param registeredFrom source of token generation.
     *
     * @return prepared line with payment information.
     */
    fun createPaymentString(
        order: String = "",
        timestamp: Long,
        uuid: String,
        cardInfo: CardInfo,
        registeredFrom: MSDKRegisteredFrom = MSDKRegisteredFrom.MSDK_CORE,
    ): String
}
