package net.payrdr.mobile.payment.sdk.form.component.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.payrdr.mobile.payment.sdk.form.component.CardInfo
import net.payrdr.mobile.payment.sdk.form.component.CardInfoProvider
import net.payrdr.mobile.payment.sdk.form.component.CardInfoProviderException
import net.payrdr.mobile.payment.sdk.form.utils.executePostJson
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import org.json.JSONObject
import java.net.URL

/**
 * Implementation of the provider of obtaining information about the card from a remote server.
 *
 * @param url method address for getting information.
 * @param urlBin prefix for getting binary data.
 */
class RemoteCardInfoProvider(
    private var url: String,
    private var urlBin: String
) : CardInfoProvider {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun resolve(bin: String): CardInfo = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject(mapOf("bin" to bin)).toString()
            val connection = URL(url).executePostJson(body)
            val info = CardInfo.fromJson(connection.responseBodyToJsonObject())
            info.copy(
                logo = urlBin + info.logo,
                logoInvert = urlBin + info.logoInvert
            )
        } catch (cause: Exception) {
            throw CardInfoProviderException("Error while load card info", cause)
        }
    }
}
