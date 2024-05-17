package net.payrdr.mobile.payment.sdk.payment.model

import net.payrdr.mobile.payment.sdk.form.component.impl.SSLContextConfig

/**
 * SDK Payment configuration options class .
 *
 * @param baseURL base URL address of the gateway to invoke payment methods.
 * @param use3DSConfig configuration for 3ds.
 * @param sslContextConfig custom SSL context object with TLS certificates.
 * in the JWS header response
 */
data class SDKPaymentConfig(
    val baseURL: String,
    val use3DSConfig: Use3DSConfig = Use3DSConfig.NoUse3ds2sdk,
    val sslContextConfig: SSLContextConfig? = null,
)
