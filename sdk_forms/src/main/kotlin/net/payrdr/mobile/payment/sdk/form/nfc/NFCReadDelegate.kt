package net.payrdr.mobile.payment.sdk.form.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.parser.EmvTemplate
import java.util.Date

/**
 * Card data scanner.
 *
 * @param nfcAdapter adapter for interaction with NFC.
 */
class NFCReadDelegate(private val nfcAdapter: NfcAdapter) {

    /**
     * A listener to track the process of reading card data via NFC.
     */
    var nfcCardListener: NFCCardListener? = null

    /**
     * Must be called in the onNewIntent Activity method, where data from NFC is processed.
     *
     * @param intent intent with data from the NFC chip.
     * @return true if [intent] matches data from the NFC chip, otherwise false.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onNewIntent(intent: Intent): Boolean {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action
        ) {
            try {
                val iTag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                val provider = NFCProvider(IsoDep.get(iTag))
                val config: EmvTemplate.Config = EmvTemplate.Config()
                    .setContactLess(true)
                    .setReadAllAids(false)
                    .setReadTransactions(false)
                    .setRemoveDefaultParsers(false)
                    .setReadAt(false)
                val parser = EmvTemplate.Builder()
                    .setProvider(provider)
                    .setConfig(config)
                    .build()

                provider.connect()
                parser.readEmvCard()?.let { card ->
                    nfcCardListener?.onCardReadSuccess(
                        number = card.cardNumber,
                        expiryDate = card.expireDate
                    )
                }
            } catch (e: Exception) {
                nfcCardListener?.onCardReadError(e)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Returns the status of the NFC operation.
     *
     * @return true if NFC is enabled on the device, otherwise false.
     */
    fun isEnabled(): Boolean = nfcAdapter.isEnabled

    /**
     * Must be called in the onResume method of the Activity, where the data from the NFC is processed.
     *
     * @param activity screen for processing data from NFC.
     * @param activityClass the screen Activity class.
     */
    fun onResume(activity: Activity, activityClass: Class<*>) {
        val updateIntent = Intent(activity, activityClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flag = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(activity, 0, updateIntent, flag)
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, arrayOf<IntentFilter>(), null)
    }

    /**
     * Must be called in the onPause Activity method, where the data from the NFC is processed.
     *
     * @param activity screen for processing data from NFC.
     */
    fun onPause(activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }

    /**
     * Interface for monitoring the status of reading card data.
     */
    interface NFCCardListener {

        /**
         * Called when the card data has been successfully read.
         *
         * @param number card number.
         * @param expiryDate card expiration date.
         */
        fun onCardReadSuccess(number: String, expiryDate: Date?)

        /**
         * Called when an error occurs during the card reading process.
         *
         * @param e error occurred.
         */
        fun onCardReadError(e: Exception)
    }
}
