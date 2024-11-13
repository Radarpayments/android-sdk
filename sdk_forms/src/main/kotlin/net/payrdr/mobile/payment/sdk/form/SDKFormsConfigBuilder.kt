package net.payrdr.mobile.payment.sdk.form

import net.payrdr.mobile.payment.sdk.form.component.CardInfoProvider
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteCardInfoProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.model.SDKConfig
import net.payrdr.mobile.payment.sdk.logs.Logger
import javax.net.ssl.SSLContext

/**
 * Constructor for forming the SDK configuration.
 */
class SDKFormsConfigBuilder {

    private var keyProvider: KeyProvider? = null

    private var cardInfoProvider: CardInfoProvider = RemoteCardInfoProvider(
        url = "https://mrbin.io/bins/display",
        urlBin = "https://mrbin.io/bins/",
    )

    /**
     * Set the remote key provider by url
     *
     * @param providerUrl url address for receiving encryption keys
     */
    fun keyProviderUrl(providerUrl: String, sslContext: SSLContext?): SDKFormsConfigBuilder = apply {
        Logger.info(
            this.javaClass,
            Constants.TAG,
            "keyProviderUrl($providerUrl): Set the remote key provider by url.",
            null
        )
        if (this.keyProvider != null) {
            Logger.error(
                this.javaClass,
                Constants.TAG,
                "keyProviderUrl($providerUrl): Error",
                SDKException("You should use only one key provider build-method")
            )
            throw SDKException("You should use only one key provider build-method")
        }
        this.keyProvider = RemoteKeyProvider(providerUrl, sslContext)
    }

    /**
     * Set the provider of the encryption key.
     *
     * @param provider the encryption key provider to use.
     */
    fun keyProvider(provider: KeyProvider): SDKFormsConfigBuilder = apply {
        Logger.info(
            this.javaClass,
            Constants.TAG,
            "keyProvider($provider): Set the provider of the encryption key.",
            null
        )
        if (this.keyProvider != null) {
            Logger.error(
                this.javaClass,
                Constants.TAG,
                "keyProvider($provider): Error",
                SDKException("You should use only one key provider build-method")
            )
            throw SDKException("You should use only one key provider build-method")
        }
        this.keyProvider = provider
    }

    /**
     * Change the provider of card information.
     *
     * @param provider Provider for getting information about the style and type of the card.
     */
    fun cardInfoProvider(provider: CardInfoProvider): SDKFormsConfigBuilder = apply {
        this.cardInfoProvider = provider
    }

    /**
     * Creates a payment SDK configuration.
     *
     * @return SDK configuration.
     */
    fun build() = SDKConfig(
        keyProvider = this.keyProvider ?: throw SDKException("You must initialize keyProvider!"),
        cardInfoProvider = this.cardInfoProvider,
    )
}
