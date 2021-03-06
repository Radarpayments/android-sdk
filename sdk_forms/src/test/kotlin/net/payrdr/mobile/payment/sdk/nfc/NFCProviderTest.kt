package net.payrdr.mobile.payment.sdk.nfc

import android.nfc.tech.IsoDep
import androidx.test.filters.SmallTest
import com.github.devnied.emvnfccard.exception.CommunicationException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.nfc.NFCProvider
import org.junit.Before
import org.junit.Test
import java.io.IOException

@SmallTest
class NFCProviderTest {

    private lateinit var nfcProvider: NFCProvider

    @MockK
    private lateinit var isoDep: IsoDep

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        nfcProvider = NFCProvider(isoDep)
    }

    @Test
    @Description("should call connect")
    fun `should call connect`() {
        every { isoDep.connect() } just runs

        nfcProvider.connect()

        verify { isoDep.connect() }
    }

    @Test(expected = CommunicationException::class)
    @Description("should wrap internal exception")
    fun `should wrap internal exception`() {
        val array = byteArrayOf()
        every { isoDep.transceive(array) } throws IOException()

        nfcProvider.transceive(array)

        verify { isoDep.transceive(array) }
    }

    @Test
    @Description("should call historical bytes")
    fun `should call historical bytes`() {
        every { isoDep.historicalBytes } returns byteArrayOf()

        nfcProvider.at

        verify { isoDep.historicalBytes }
    }
}
