package net.payrdr.mobile.payment.sdk.form.component.impl

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.payrdr.mobile.payment.sdk.core.component.CryptogramCipher
import net.payrdr.mobile.payment.sdk.core.component.PaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider

/**
 * Implementation of a processor for forming a cryptogram.
 *
 * @param keyProvider encryption key provider.
 * @param paymentStringProcessor pay line generation processor.
 */
class DefaultCryptogramProcessor(
    private val keyProvider: KeyProvider,
    private val paymentStringProcessor: PaymentStringProcessor,
    private val cryptogramCipher: CryptogramCipher
) : CryptogramProcessor {

    override suspend fun create(
        order: String,
        timestamp: Long,
        uuid: String,
        cardInfo: CardInfo,
        registeredFrom: MSDKRegisteredFrom,
    ): String = withContext(Dispatchers.IO) {
        val key = keyProvider.provideKey()
        val paymentString = paymentStringProcessor.createPaymentString(
            order = order,
            timestamp = timestamp,
            uuid = uuid,
            cardInfo = cardInfo,
            registeredFrom = registeredFrom,
        )
        cryptogramCipher.encode(paymentString, key)
    }

    override suspend fun create(googlePayToken: String): String = withContext(Dispatchers.IO) {
        Base64.encodeToString(googlePayToken.toByteArray(), Base64.NO_WRAP)
    }
}
