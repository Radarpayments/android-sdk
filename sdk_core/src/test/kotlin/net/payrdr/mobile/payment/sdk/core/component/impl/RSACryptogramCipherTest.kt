package net.payrdr.mobile.payment.sdk.core.component.impl

import android.util.Base64
import androidx.test.filters.SmallTest
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.model.Key
import org.junit.After
import org.junit.Before
import org.junit.Test

@SmallTest
class RSACryptogramCipherTest {

    private lateinit var cipher: RSACryptogramCipher

    @Before
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<ByteArray>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<ByteArray>())
        }
        cipher = RSACryptogramCipher()
    }

    @After
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    @Test(expected = IllegalStateException::class)
    @Description("non-RSA protocol fails the check() precondition before any crypto work")
    fun `rejects non-RSA protocol`() {
        val key = Key(
            value = "-----BEGIN PUBLIC KEY-----\nirrelevant\n-----END PUBLIC KEY-----",
            protocol = "AES",
            expiration = 0L
        )
        cipher.encode("data", key)
    }

    @Test(expected = Exception::class)
    @Description("malformed PEM key material throws during KeyFactory.generatePublic")
    fun `throws on malformed key`() {
        val key = Key(
            value = "-----BEGIN PUBLIC KEY-----\nnot-a-real-key\n-----END PUBLIC KEY-----",
            protocol = "RSA",
            expiration = 0L
        )
        cipher.encode("data", key)
    }
}
