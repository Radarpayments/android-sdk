package net.payrdr.mobile.payment.sdk.payment.model

/**
 * Configuration for checking 3DS transactions.
 */
sealed class Use3DSConfig {

    /**
     * Do not use 3DS2 sdk.
     */
    object NoUse3ds2sdk: Use3DSConfig()

    /**
     * Use 3DS2 sdk.
     *
     * @param dsRoot root certificate in base64 format. Used to validate the chain of certificates.
     */
    data class Use3ds2sdk(
        val dsRoot: String,
    ): Use3DSConfig()
}
