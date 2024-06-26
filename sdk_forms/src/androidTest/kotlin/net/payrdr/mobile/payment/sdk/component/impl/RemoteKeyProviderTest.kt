package net.payrdr.mobile.payment.sdk.component.impl

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider
import net.payrdr.mobile.payment.sdk.form.component.KeyProviderException
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.SSLContextCustomCAFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okhttp3.tls.certificatePem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.net.InetAddress

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
@Suppress("MaxLineLength")
class RemoteKeyProviderTest {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var keyProvider: KeyProvider
    private val server: MockWebServer = MockWebServer()

    @Before
    fun setUp() {
        server.start()
        val localhost: String = InetAddress.getByName("localhost").canonicalHostName
        val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
            .addSubjectAlternativeName(localhost)
            .build()

        val serverCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhostCertificate)
            .build()
        val server = MockWebServer()
        server.useHttps(serverCertificates.sslSocketFactory(), false)

        val pem = localhostCertificate.certificate.certificatePem()
        val sslContext = SSLContextCustomCAFactory.fromPem(pem)

        keyProvider = RemoteKeyProvider(server.url("/").toString(), sslContext.sslContext)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    @Description("shouldReturnFirstActiveKey")
    fun shouldReturnFirstActiveKey() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "keys": [
                            {
                                "keyValue": "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAij/G3JVV3TqYCFZTPmwi4JduQMsZ2HcFLBBA9fYAvApv3FtA+zKdUGgKh/OPbtpsxe1C57gIaRclbzMoafTb0eOdj+jqSEJMlVJYSiZ8Hn6g67evhu9wXh5ZKBQ1RUpqL36LbhYnIrP+TEGR/VyjbC6QTfaktcRfa8zRqJczHFsyWxnlfwKLfqKz5wSqXkShcrwcfRJCyDRjZX6OFUECHsWVK3WMcOV3WZREwbCkh/o5R5Vl6xoyLvSqVEKQiHupJcZu9UEOJiP3yNCn9YPgyFs2vrCeg6qxDPFnCfetcDCLjjLenGF7VyZzBJ9G2NP3k/mNVtD8Kl7lpiurwY7EZwIDAQAB-----END PUBLIC KEY-----",
                                "protocolVersion": "RSA",
                                "keyExpiration": 1598527672000
                            },
                            {
                                "keyValue": "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhjH8R0jfvvEJwAHRhJi2Q4fLi1p2z10PaDMIhHbD3fp4OqypWaE7p6n6EHig9qnwC/4U7hCiOCqY6uYtgEoDHfbNA87/X0jV8UI522WjQH7Rgkmgk35r75G5m4cYeF6OvCHmAJ9ltaFsLBdr+pK6vKz/3AzwAc/5a6QcO/vR3PHnhE/qU2FOU3Vd8OYN2qcw4TFvitXY2H6YdTNF4YmlFtj4CqQoPL1u/uI0UpsG3/epWMOk44FBlXoZ7KNmJU29xbuiNEm1SWRJS2URMcUxAdUfhzQ2+Z4F0eSo2/cxwlkNA+gZcXnLbEWIfYYvASKpdXBIzgncMBro424z/KUr3QIDAQAB-----END PUBLIC KEY-----",
                                "protocolVersion": "RSA",
                                "keyExpiration": 1661599747000
                            }
                        ]
                    }
                """.trimIndent()
            )
        )

        val key = keyProvider.provideKey()

        assertEquals(
            "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAij/G3JVV3TqYCFZTPmwi4JduQMsZ2HcFLBBA9fYAvApv3FtA+zKdUGgKh/OPbtpsxe1C57gIaRclbzMoafTb0eOdj+jqSEJMlVJYSiZ8Hn6g67evhu9wXh5ZKBQ1RUpqL36LbhYnIrP+TEGR/VyjbC6QTfaktcRfa8zRqJczHFsyWxnlfwKLfqKz5wSqXkShcrwcfRJCyDRjZX6OFUECHsWVK3WMcOV3WZREwbCkh/o5R5Vl6xoyLvSqVEKQiHupJcZu9UEOJiP3yNCn9YPgyFs2vrCeg6qxDPFnCfetcDCLjjLenGF7VyZzBJ9G2NP3k/mNVtD8Kl7lpiurwY7EZwIDAQAB-----END PUBLIC KEY-----",
            key.value
        )
        assertEquals("RSA", key.protocol)
        assertEquals(1598527672000L, key.expiration)
        Unit
    }

    @Test(expected = KeyProviderException::class)
    @Description("shouldReturnKeyProviderExceptionForIncorrectResponseBody")
    fun shouldReturnKeyProviderExceptionForIncorrectResponseBody() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                "Incorrect body response"
            )
        )

        keyProvider.provideKey()
        Unit
    }

    @Test(expected = KeyProviderException::class)
    @Description("shouldReturnKeyProviderExceptionForErrorCodeResponse")
    fun shouldReturnKeyProviderExceptionForErrorCodeResponse() = runBlocking {
        server.enqueue(
            MockResponse().setHttp2ErrorCode(500)
        )

        keyProvider.provideKey()
        Unit
    }

    @Test
    fun shouldTrustToCustomCACertificate() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                    {
                        "keys": [{
                            "keyValue": "-----BEGIN PUBLIC KEY-----FIRST KEY-----END PUBLIC KEY-----",
                            "protocolVersion": "RSA",
                            "keyExpiration": 1960710807000
                        }, {
                            "keyValue": "-----BEGIN PUBLIC KEY-----SECOND KEY-----END PUBLIC KEY-----",
                            "protocolVersion": "RSA",
                            "keyExpiration": 1960710864000
                        }]
                    }
                """.trimIndent()
            )
        )

        val key = keyProvider.provideKey()

        assertEquals(key.protocol, "RSA")
        assertEquals(key.expiration, 1960710807000)
        assertEquals(key.value, "-----BEGIN PUBLIC KEY-----FIRST KEY-----END PUBLIC KEY-----")
    }
}
