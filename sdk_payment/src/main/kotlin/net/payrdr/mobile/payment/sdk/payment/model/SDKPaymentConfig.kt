package net.payrdr.mobile.payment.sdk.payment.model

import net.payrdr.mobile.payment.sdk.form.component.impl.SSLContextConfig

/**
 * SDK Payment configuration options class .
 *
 * @param baseURL base URL address of the gateway to invoke payment methods.
 * @param sslContextConfig custom SSL context object with TLS certificates.
 * in the JWS header response
 */
data class SDKPaymentConfig(
    val baseURL: String,
    val sslContextConfig: SSLContextConfig? = null,
)
