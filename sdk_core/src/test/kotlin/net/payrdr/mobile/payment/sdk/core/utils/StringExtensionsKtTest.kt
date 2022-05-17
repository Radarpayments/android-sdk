package net.payrdr.mobile.payment.sdk.core.utils

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.junit.Test

@SmallTest
class StringExtensionsKtTest {

    @Test
    fun `pemKeyContent should remove spaces header and footer`() {
        val pem = """
        -----BEGIN PUBLIC KEY-----
        Content
        -----END PUBLIC KEY-----
        """.trimIndent()

        val content = pem.pemKeyContent()

        content shouldBe "Content"
    }
}
