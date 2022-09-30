package net.payrdr.mobile.payment.sdk.form.component.impl

import android.util.Base64
import net.payrdr.mobile.payment.sdk.core.utils.pemKeyContent
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * Factory for creating an SSLContext containing the user's CA certificate.
 */
object SSLContextCustomCAFactory {

    /**
     * Creates SSLContext with CA certificate read from input parameter.
     *
     * @param pem containing the certificate in pem format.
     *
     * Example:
     *
     * -----BEGIN CERTIFICATE-----
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAij/G3JVV3TqYCFZTPmwi
     * ...
     * -----END CERTIFICATE-----
     *
     * @return sslContext with CA certificate from [pem]
     */
    fun fromPem(pem: String): SSLContext {
        val base64cert = pem.pemKeyContent()
        return fromBase64String(base64cert)
    }

    /**
     * Creates SSLContext with CA certificate read from input parameter.
     *
     * @param base64cert containing only base64 part of the certificate.
     *
     * Example:
     *
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAij/G3JVV3TqYCFZTPmwi
     * ...
     *
     * @return sslContext with CA certificate from [base64cert]
     */
    fun fromBase64String(base64cert: String): SSLContext {
        val decoded = Base64.decode(base64cert, Base64.NO_WRAP)
        val caInput = ByteArrayInputStream(decoded)
        return fromInputStream(caInput)
    }

    /**
     * Creates SSLContext with CA certificate read from input parameter.
     *
     * @param caInput - input stream with .crt file content.
     *
     * @return sslContext with CA certificate from [caInput]
     */
    fun fromInputStream(caInput: InputStream): SSLContext {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val ca = caInput.use { cf.generateCertificate(it) }
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(keyStore)
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        return sslContext
    }
}
