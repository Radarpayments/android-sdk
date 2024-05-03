package net.payrdr.mobile.payment.sdk.core.component.impl

import net.payrdr.mobile.payment.sdk.core.component.PaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
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
     * • timestamp(1)/uuid(2)/pan(3)/cvv(4)/expdate(5)/mdOrder(6)/bindingId(7)/cardholder(8)/registeredFrom(9)
     *
     * @param order order identifier.
     * @param timestamp request date.
     * @param uuid identifier in UUID standard.
     * @param cardInfo information about the withdrawal card.
     * @param registeredFrom source of token generation.
     *
     */
    override fun createPaymentString(
        order: String,
        timestamp: Long,
        uuid: String,
        cardInfo: CardInfo,
        registeredFrom: MSDKRegisteredFrom,
    ): String {
        val cardIdentifier = cardInfo.identifier
        val sb = StringBuilder().apply {
            // timestamp(1)
            append(Date(timestamp).format())
            append(SPLASH)
            // uuid(2)
            append(uuid)
            append(SPLASH)
            // pan(3)
            append(if (cardIdentifier is CardPanIdentifier) cardIdentifier.value else "")
            append(SPLASH)
            // cvv(4)
            append(cardInfo.cvv ?: "")
            append(SPLASH)
            // expdate(5)
            append(cardInfo.expDate?.format() ?: "")
            append(SPLASH)
            // mdOrder(6)
            append(order)
            append(SPLASH)
            // bindingId(7)
            append(if (cardIdentifier is CardBindingIdIdentifier) cardIdentifier.value else "")
            append(SPLASH)
            // cardholder(8)
            append(cardInfo.cardHolder ?: "")
            append(SPLASH)
            // registeredFrom(9)
            append(registeredFrom.registeredFromValue)
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
