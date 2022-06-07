package net.payrdr.mobile.payment.sdk.nfc

import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.test.filters.SmallTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.nfc.NFCReadDelegate
import org.junit.Before
import org.junit.Test

@SmallTest
class NFCReadDelegateTest {

    private lateinit var nfcReadDelegate: NFCReadDelegate

    @MockK
    private lateinit var nfcAdapter: NfcAdapter

    @MockK
    private lateinit var activity: AppCompatActivity

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        nfcReadDelegate = NFCReadDelegate(nfcAdapter)
    }

    @Test
    @Description("should call pause")
    fun `should call pause`() {
        every { nfcAdapter.disableForegroundDispatch(activity) } just runs

        nfcReadDelegate.onPause(activity)

        verify { nfcAdapter.disableForegroundDispatch(activity) }
    }

    @Test
    @Description("should call is enabled")
    fun `should call is enabled`() {
        every { nfcAdapter.isEnabled } returns true

        nfcReadDelegate.isEnabled()

        verify { nfcAdapter.isEnabled }
    }
}
