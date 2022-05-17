package net.payrdr.mobile.payment.sdk.form.nfc

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.exception.CommunicationException
import com.github.devnied.emvnfccard.parser.IProvider
import java.io.IOException

/**
 * Provider for transmitting commands to the card chip.
 *
 * @param isoDep object for sending commands to the card chip.
 */
class NFCProvider(private val isoDep: IsoDep) : IProvider {

    /**
     * Connect.
     */
    fun connect() {
        isoDep.connect()
    }

    override fun transceive(pCommand: ByteArray?): ByteArray = try {
        isoDep.transceive(pCommand)
    } catch (e: IOException) {
        throw CommunicationException(e.message)
    }

    override fun getAt(): ByteArray = isoDep.historicalBytes
}
