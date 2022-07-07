package net.payrdr.mobile.payment.sdk.payment.model

/**
 * SDK Payment configuration options class .
 *
 * @param baseURL base URL address of the gateway to invoke payment methods.
 * @param dsRoot root certificate in base64 format. Used to validate the chain of certificates
 * in the JWS header response.
 *
 */
data class SDKPaymentConfig(
    val baseURL: String,
    val dsRoot: String,
)
