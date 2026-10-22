package net.payrdr.mobile.payment.sdk.payment

import android.content.Context
import android.util.Base64
import net.payrdr.mobile.payment.sdk.Constants
import net.payrdr.mobile.payment.sdk.component.impl.SSLContextCustomCAFactory
import net.payrdr.mobile.payment.sdk.logs.Logger
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig

/**
 * Keeps [SDKPaymentConfig] on disk so that SDK screens recreated by Android after process death
 * can continue the payment before the host app calls SDKPayment.init() again.
 *
 * Only public data is stored: the gateway URL and the merchant's CA certificate. The data lives only while
 * a payment is in progress: it is saved on checkout and cleared when the payment returns its result.
 */
internal object SDKPaymentConfigStorage {
    internal const val PREFS_NAME = "net.payrdr.mobile.payment.sdk.payment_config"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_CA_CERTIFICATE = "ca_certificate"
    private const val KEY_UNRESTORABLE_SSL_CONTEXT = "unrestorable_ssl_context"

    fun save(context: Context, config: SDKPaymentConfig) {
        val sslContextConfig = config.sslContextConfig
        val certificate = sslContextConfig?.customCertificate?.encoded
            ?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        prefs(context).edit()
            .putString(KEY_BASE_URL, config.baseURL)
            .putString(KEY_CA_CERTIFICATE, certificate)
            // A custom SSLContext (e.g. with pinning) built without a certificate cannot be stored.
            .putBoolean(KEY_UNRESTORABLE_SSL_CONTEXT, sslContextConfig != null && certificate == null)
            .apply()
    }

    /**
     * @return the saved configuration, or null if there is nothing to restore or it cannot be restored safely.
     */
    @Suppress("TooGenericExceptionCaught")
    fun restore(context: Context): SDKPaymentConfig? {
        val prefs = prefs(context)
        val baseUrl = prefs.getString(KEY_BASE_URL, null)
        // Fail closed: continuing without the merchant's SSLContext would silently drop pinning.
        if (baseUrl == null || prefs.getBoolean(KEY_UNRESTORABLE_SSL_CONTEXT, false)) return null
        return try {
            val sslContextConfig = prefs.getString(KEY_CA_CERTIFICATE, null)
                ?.let { SSLContextCustomCAFactory.fromBase64String(it) }
            SDKPaymentConfig(baseURL = baseUrl, sslContextConfig = sslContextConfig)
        } catch (e: Exception) {
            Logger.error(javaClass, Constants.TAG, "restore(): saved CA certificate cannot be loaded", e)
            clear(context)
            null
        }
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
