package net.payrdr.mobile.payment.sdk.form.component.impl

import android.util.Base64
import net.payrdr.mobile.payment.sdk.core.utils.pemKeyContent
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.Enumeration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * Factory for creating an SSLContext containing the user's CA certificate.
 */
object SSLContextCustomCAFactory {
    private const val SYSTEM_KEY_STORE_NAME = "AndroidCAStore"
    private var customKeyStore: KeyStore? = null

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
     * @return SSLContextConfiguration object with CA certificate from [pem]
     */
    fun fromPem(pem: String): SSLContextConfig {
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
     * @return SSLContextConfiguration object with CA certificate from [base64cert]
     */
    fun fromBase64String(base64cert: String): SSLContextConfig {
        val decoded = Base64.decode(base64cert, Base64.NO_WRAP)
        val caInput = ByteArrayInputStream(decoded)
        return fromInputStream(caInput)
    }

    /**
     * Creates SSLContext with CA certificate read from input parameter.
     *
     * @param caInput - input stream with .crt file content.
     *
     * @return SSLContextConfiguration object with CA certificate from [caInput]
     */
    fun fromInputStream(caInput: InputStream): SSLContextConfig {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val ca = caInput.use { cf.generateCertificate(it) }
        getCustomKeyStore().setCertificateEntry("ca", ca)
        loadSystemCAToCustomKeyStore()
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(getCustomKeyStore())
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        return SSLContextConfig(
            sslContext = sslContext,
            trustManager = tmf.trustManagers.first(),
        )
    }

    /**
     * Uploads system certificates to custom Key Store.
     * This is necessary in order to try to get a trusted connection if the custom certificate does not work.
     */
    @Suppress("TooGenericExceptionCaught", "NestedBlockDepth")
    private fun loadSystemCAToCustomKeyStore() {
        val systemCAKeyStore = KeyStore.getInstance(SYSTEM_KEY_STORE_NAME)
        systemCAKeyStore?.let {
            systemCAKeyStore.load(null, null)
            val keyAliases: Enumeration<String> = systemCAKeyStore.aliases()
            while (keyAliases.hasMoreElements()) {
                val alias: String = keyAliases.nextElement()
                val cert: Certificate = systemCAKeyStore.getCertificate(alias)
                try {
                    if (!getCustomKeyStore().containsAlias(alias)) {
                        getCustomKeyStore().setCertificateEntry(alias, cert)
                    }
                } catch (e: Exception) {
                    throw IllegalAccessException("Can't load system CA to custom Key Store")
                }
            }
        }
    }

    /**
     * Implements customKeyStore object.
     *
     * @return KeyStore object [customKeyStore] field.
     */
    private fun getCustomKeyStore(): KeyStore {
        if (customKeyStore == null) {
            customKeyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null, null)
            }
        }
        return customKeyStore!!
    }
}
