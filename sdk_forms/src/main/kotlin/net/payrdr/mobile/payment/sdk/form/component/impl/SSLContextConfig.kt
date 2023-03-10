package net.payrdr.mobile.payment.sdk.form.component.impl

import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

/**
 * SSL context configuration.
 *
 * @param sslContext sslContext with CA certificate.
 * @param trustManager trustManager.
 * @param customCertificate custom certificate. Required for validation in WebView.
 */
data class SSLContextConfig(
    val sslContext: SSLContext,
    val trustManager: TrustManager,
    val customCertificate: X509Certificate? = null,
)
