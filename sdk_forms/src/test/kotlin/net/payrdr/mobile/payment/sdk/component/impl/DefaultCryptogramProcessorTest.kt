package net.payrdr.mobile.payment.sdk.component.impl

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.core.component.CryptogramCipher
import net.payrdr.mobile.payment.sdk.core.component.PaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.model.Key
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.DefaultCryptogramProcessor
import org.junit.Before
import org.junit.Test

@SmallTest
class DefaultCryptogramProcessorTest {

    private lateinit var defaultCryptogramProcessor: DefaultCryptogramProcessor

    @MockK
    private lateinit var keyProvider: KeyProvider

    @MockK
    private lateinit var paymentStringProcessor: PaymentStringProcessor

    @MockK
    private lateinit var cryptogramCipher: CryptogramCipher

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        defaultCryptogramProcessor =
            DefaultCryptogramProcessor(
                keyProvider = keyProvider,
                paymentStringProcessor = paymentStringProcessor,
                cryptogramCipher = cryptogramCipher
            )
    }

    @Test
    fun `should create payment cryptogram`() {
        val key = Key(
            value = "",
            protocol = "",
            expiration = 1598689996644
        )
        runBlocking {
            coEvery { keyProvider.provideKey() } returns key
            every {
                paymentStringProcessor.createPaymentString(
                    order = "413519e0-c625-468b-a250-698ce1d94126",
                    uuid = "71bded36-ad00-41cd-aa33-3f723dfafe81",
                    timestamp = 1598682006644,
                    cardInfo = CardInfo(
                        identifier = CardPanIdentifier(
                            value = "123456789012"
                        )
                    )
                )
            } returns "paymentStringValue"
            coEvery {
                cryptogramCipher.encode("paymentStringValue", key)
            } returns "cryptogramValue"

            val cryptogram = defaultCryptogramProcessor.create(
                order = "413519e0-c625-468b-a250-698ce1d94126",
                uuid = "71bded36-ad00-41cd-aa33-3f723dfafe81",
                timestamp = 1598682006644,
                cardInfo = CardInfo(
                    identifier = CardPanIdentifier(
                        value = "123456789012"
                    )
                )
            )

            cryptogram shouldBe "cryptogramValue"
        }
    }
}
