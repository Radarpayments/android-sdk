package net.payrdr.mobile.payment.sdk.core.component.impl

import net.payrdr.mobile.payment.sdk.core.component.PaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of a processor for generating a line with payment information using a template.
 */
class DefaultPaymentStringProcessor : PaymentStringProcessor {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)

    /**
     * Generates a list of payment data using a template.
     *
     * Available template options :
     *
     * • timestamp/UUID/PAN/CVV/EXPDATE/mdOrder
     * • timestamp/UUID/PAN//EXPDATE/mdOrder
     * • timestamp/UUID/PAN///mdOrder
     * • timestamp/UUID//CVV/mdOrder/bindingId
     * • timestamp/UUID///mdOrder/bindingId
     *
     * @param order order identifier.
     * @param timestamp request date.
     * @param uuid identifier in UUID standard.
     * @param cardInfo information about the withdrawal card.
     *
     */
    override fun createPaymentString(
        order: String,
        timestamp: Long,
        uuid: String,
        cardInfo: CardInfo
    ): String {
        val cardIdentifier = cardInfo.identifier
        val bindingId: String =
            if (cardIdentifier is CardBindingIdIdentifier) cardIdentifier.value else ""
        val sb = StringBuilder().apply {
            append(Date(timestamp).format())
            append(SPLASH)
            append(uuid)
            append(SPLASH)
            append(if (cardIdentifier is CardPanIdentifier) cardIdentifier.value else "")
            append(SPLASH)
            append(cardInfo.cvv ?: "")
            append(SPLASH)
            append(cardInfo.expDate?.format() ?: "")
            append(SPLASH)
            append(order)
            if (bindingId.isNotBlank()) {
                append(SPLASH)
                append(bindingId)
            }
        }
        return sb.toString()
    }

    private fun ExpiryDate.format(): String = "$expYear$expMonth"

    private fun Date.format(): String = dateFormatter.format(this).let {
        it.substring(0, it.length - 2) + ':' + it.substring(it.length - 2)
    }

    companion object {
        private const val SPLASH = "/"
    }
}
