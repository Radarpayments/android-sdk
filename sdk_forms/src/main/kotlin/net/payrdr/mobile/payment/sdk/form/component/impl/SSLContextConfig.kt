package net.payrdr.mobile.payment.sdk.form.component.impl

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

/**
 * SSL context configuration.
 *
 * @param sslContext sslContext with CA certificate.
 * @param trustManager trustManager.
 */
data class SSLContextConfig(
    val sslContext: SSLContext,
    val trustManager: TrustManager,
)
