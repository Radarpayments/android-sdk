package net.payrdr.mobile.payment.sdk.core.model

/**
 * SDK core configuration option class for token creation.
 *
 * @param paymentCardParams information about the linked card.
 * @param timestamp the timestamp used in the generated token.
 * @param registeredFrom source of token generation.
 */

data class SDKCoreConfig(
    val paymentCardParams: PaymentCardParams,
    val timestamp: Long = System.currentTimeMillis(),
    val registeredFrom: MSDKRegisteredFrom = MSDKRegisteredFrom.MSDK_CORE
)

/**
 * Information about card.
 *
 * @param pubKey public key.
 */

sealed class PaymentCardParams(open val pubKey: String) {

    /**
     * Information about stored card.
     *
     *  @param cvc secret code for card.
     *  @param pubKey public key.
     */

    sealed class StoredCardPaymentParams(
        open val cvc: String?,
        override val pubKey: String
    ) : PaymentCardParams(pubKey = pubKey)

    /**
     * Information about new card.
     *
     *  @param pan card number.
     *  @param cvc secret crd code.
     *  @param expiryMMYY expiry date for card.
     *  @param cardHolder first and last name of cardholder.
     *  @param pubKey public key.
     */

    sealed class NewCardPaymentParams(
        open val pan: String,
        open val cvc: String,
        open val expiryMMYY: String,
        open val cardHolder: String?,
        override val pubKey: String
    ) : PaymentCardParams(pubKey = pubKey)
}
