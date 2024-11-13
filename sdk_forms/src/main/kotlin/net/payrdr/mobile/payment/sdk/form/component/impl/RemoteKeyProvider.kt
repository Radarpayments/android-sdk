package net.payrdr.mobile.payment.sdk.form.component.impl

import java.net.URL
import javax.net.ssl.SSLContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.payrdr.mobile.payment.sdk.core.model.Key
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider
import net.payrdr.mobile.payment.sdk.form.component.KeyProviderException
import net.payrdr.mobile.payment.sdk.form.utils.asList
import net.payrdr.mobile.payment.sdk.form.utils.executeGet
import net.payrdr.mobile.payment.sdk.form.utils.requiredField
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import net.payrdr.mobile.payment.sdk.logs.Logger
import org.json.JSONObject

/**
 * Key provider based on the external url of the resource.
 *
 * @param url the address of the remote server providing an active encryption key.
 * @param sslContext SSLContext with a custom SSL certificate.
 */
class RemoteKeyProvider(
    private var url: String,
    private var sslContext: SSLContext? = null,
) : KeyProvider {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun provideKey(): Key = withContext(Dispatchers.IO) {
        val keys = try {
            Logger.info(
                this.javaClass,
                Constants.TAG,
                "provideKey(): Key provider based on the external url of the resource.",
                null
            )
            val connection = URL(url).executeGet(sslContext)
             ActiveKeysDto.fromJson(connection.responseBodyToJsonObject()).keys.map { it.toKey() }
        } catch (cause: Exception) {
            Logger.error(
                this.javaClass,
                Constants.TAG,
                "provideKey(): Error",
                KeyProviderException("Error while load active keys", cause)
            )
            throw KeyProviderException("Error while load active keys", cause)
        }

        if (keys.isEmpty()) {
            Logger.error(
                this.javaClass,
                Constants.TAG,
                "provideKey(): Error",
                KeyProviderException("Keys for tokens are not configured on remote server", null)
            )
            throw KeyProviderException("Keys for tokens are not configured on remote server", null)
        } else {
            keys.first()
        }
    }

    private fun ActiveKeyDto.toKey() = Key(
        value = this.keyValue.requiredField("keyValue"),
        protocol = this.protocolVersion.requiredField("protocolVersion"),
        expiration = this.keyExpiration.requiredField("keyExpiration")
    )

    private data class ActiveKeysDto(
        val keys: List<ActiveKeyDto>
    ) {

        companion object {

            fun fromJson(jsonObject: JSONObject): ActiveKeysDto = ActiveKeysDto(
                keys = jsonObject.getJSONArray("keys").asList().map {
                    ActiveKeyDto.fromJson(it)
                }
            )
        }
    }

    private data class ActiveKeyDto(
        val keyValue: String,
        val protocolVersion: String,
        val keyExpiration: Long
    ) {
        companion object {

            fun fromJson(jsonObject: JSONObject): ActiveKeyDto = ActiveKeyDto(
                keyValue = jsonObject.getString("keyValue"),
                protocolVersion = jsonObject.getString("protocolVersion"),
                keyExpiration = jsonObject.getLong("keyExpiration")
            )
        }
    }
}
