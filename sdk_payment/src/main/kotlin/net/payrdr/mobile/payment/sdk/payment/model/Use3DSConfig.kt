package net.payrdr.mobile.payment.sdk.payment.model

/**
 * Configuration for checking 3DS transactions.
 */
sealed class Use3DSConfig {

    /**
     * Use only 3DS1 with web challenge flow.
     */
    object Use3DS1: Use3DSConfig()

    /**
     * Use 3DS2.
     *
     * @param dsRoot root certificate in base64 format. Used to validate the chain of certificates.
     */
    data class Use3DS2(
        val dsRoot: String,
    ): Use3DSConfig()
}
