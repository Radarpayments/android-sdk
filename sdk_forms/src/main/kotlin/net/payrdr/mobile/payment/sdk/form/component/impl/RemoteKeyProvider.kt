package net.payrdr.mobile.payment.sdk.form.component.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.payrdr.mobile.payment.sdk.core.model.Key
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider
import net.payrdr.mobile.payment.sdk.form.component.KeyProviderException
import net.payrdr.mobile.payment.sdk.form.utils.asList
import net.payrdr.mobile.payment.sdk.form.utils.executeGet
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import org.json.JSONObject
import java.net.URL

/**
 * Key provider based on the external url of the resource.
 *
 * @param url the address of the remote server providing an active encryption key.
 */
class RemoteKeyProvider(private var url: String) : KeyProvider {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun provideKey(): Key = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).executeGet()
            val keys = ActiveKeysDto.fromJson(connection.responseBodyToJsonObject()).keys
            keys.first().toKey()
        } catch (cause: Exception) {
            throw KeyProviderException("Error while load active keys", cause)
        }
    }

    private fun ActiveKeyDto.toKey() = Key(
        value = this.keyValue,
        protocol = this.protocolVersion,
        expiration = this.keyExpiration
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
