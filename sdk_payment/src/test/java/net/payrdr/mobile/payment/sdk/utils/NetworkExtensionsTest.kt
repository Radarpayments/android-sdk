package net.payrdr.mobile.payment.sdk.utils

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test
import java.net.URL
import javax.net.ssl.SSLContext

@SmallTest
class NetworkExtensionsTest {

    @Test
    @Description("GET connection is created with connect and read timeouts")
    fun `executeGet sets request timeouts`() {
        val connection = URL("https://example.com/payment/se/keys.do").executeGet()

        connection.connectTimeout shouldBe CONNECT_TIMEOUT_MS
        connection.readTimeout shouldBe READ_TIMEOUT_MS
    }

    @Test
    @Description("timeouts are applied together with a custom SSL context")
    fun `executeGet with ssl context sets request timeouts`() {
        val sslContext = SSLContext.getDefault()

        val connection = URL("https://example.com/payment/se/keys.do").executeGet(sslContext)

        connection.connectTimeout shouldBe CONNECT_TIMEOUT_MS
        connection.readTimeout shouldBe READ_TIMEOUT_MS
    }

    @Test
    @Description("timeouts match the iOS SDK request timeout of 30 seconds")
    fun `timeouts are thirty seconds`() {
        CONNECT_TIMEOUT_MS shouldBe 30_000
        READ_TIMEOUT_MS shouldBe 30_000
    }
}
