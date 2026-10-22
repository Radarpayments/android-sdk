package net.payrdr.mobile.payment.sdk.component.impl

import android.util.Base64
import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.payrdr.mobile.payment.sdk.core.utils.pemKeyContent
import okhttp3.tls.HeldCertificate
import org.junit.Test
import java.io.ByteArrayInputStream
import javax.net.ssl.X509TrustManager

@SmallTest
class SSLContextCustomCAFactoryTest {

    private fun buildTestCertificatePem(): String =
        HeldCertificate.Builder()
            .commonName("payrdr-test")
            .build()
            .certificatePem()

    @Test
    fun fromInputStreamShouldProduceConfigWithTlsContextAndX509TrustManager() {
        val pem = buildTestCertificatePem()

        val config = SSLContextCustomCAFactory.fromInputStream(
            ByteArrayInputStream(pem.toByteArray())
        )

        config.sslContext.protocol shouldBe "TLS"
        (config.trustManager is X509TrustManager) shouldBe true
        config.customCertificate shouldNotBe null
    }

    @Test
    fun fromInputStreamShouldExposeLoadedCertificateAsCustomCertificate() {
        val pem = buildTestCertificatePem()

        val config = SSLContextCustomCAFactory.fromInputStream(
            ByteArrayInputStream(pem.toByteArray())
        )

        // The loaded X509Certificate subject should match the CN we requested.
        val subject = config.customCertificate?.subjectDN?.name
        (subject?.contains("payrdr-test") ?: false) shouldBe true
    }

    @Test
    fun fromBase64StringShouldMatchFromInputStream() {
        val pem = buildTestCertificatePem()
        val base64 = pem.pemKeyContent()

        val fromBase64 = SSLContextCustomCAFactory.fromBase64String(base64)
        val fromStream = SSLContextCustomCAFactory.fromInputStream(
            ByteArrayInputStream(Base64.decode(base64, Base64.NO_WRAP))
        )

        fromBase64.customCertificate shouldBe fromStream.customCertificate
    }

    @Test
    fun fromPemShouldMatchFromBase64String() {
        val pem = buildTestCertificatePem()
        val base64 = pem.pemKeyContent()

        val fromPem = SSLContextCustomCAFactory.fromPem(pem)
        val fromBase64 = SSLContextCustomCAFactory.fromBase64String(base64)

        fromPem.customCertificate shouldBe fromBase64.customCertificate
    }

    @Test
    fun trustManagerShouldAcceptServerChainSignedByLoadedCertificate() {
        val ca = HeldCertificate.Builder()
            .commonName("payrdr-ca")
            .certificateAuthority(0)
            .build()
        val server = HeldCertificate.Builder()
            .commonName("payrdr-server")
            .signedBy(ca)
            .build()

        val config = SSLContextCustomCAFactory.fromInputStream(
            ByteArrayInputStream(ca.certificatePem().toByteArray())
        )

        val tm = config.trustManager as X509TrustManager
        // Should not throw.
        tm.checkServerTrusted(
            arrayOf(server.certificate, ca.certificate),
            "RSA"
        )
    }
}
