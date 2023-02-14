package net.payrdr.mobile.payment.sdk.form.component.impl

import java.net.URL
import javax.net.ssl.SSLContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.payrdr.mobile.payment.sdk.core.Logger
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.component.CardInfo
import net.payrdr.mobile.payment.sdk.form.component.CardInfoProvider
import net.payrdr.mobile.payment.sdk.form.component.CardInfoProviderException
import net.payrdr.mobile.payment.sdk.form.utils.executePostJson
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import org.json.JSONObject

/**
 * Implementation of the provider of obtaining information about the card from a remote server.
 *
 * @param url method address for getting information.
 * @param urlBin prefix for getting binary data.
 * @param sslContext SSLContext with a custom SSL certificate.
 */
class RemoteCardInfoProvider(
    private val url: String,
    private val urlBin: String,
    private val sslContext: SSLContext? = null,
) : CardInfoProvider {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun resolve(bin: String): CardInfo = withContext(Dispatchers.IO) {
        try {
            Logger.log(
                this.javaClass,
                Constants.TAG,
                "resolve($bin):",
                null
            )
            val body = JSONObject(mapOf("bin" to bin)).toString()
            val connection = URL(url).executePostJson(body, sslContext)
            val info = CardInfo.fromJson(connection.responseBodyToJsonObject())
            info.copy(
                logoMini = urlBin + info.logoMini
            )
        } catch (cause: Exception) {
            Logger.log(
                this.javaClass,
                Constants.TAG,
                "resolve($bin): Error",
                CardInfoProviderException("Error while load card info", cause)
            )
            throw CardInfoProviderException("Error while load card info", cause)
        }
    }
}
