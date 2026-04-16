package net.payrdr.mobile.payment.sdk.model

import net.payrdr.mobile.payment.sdk.component.CardInfoProvider
import net.payrdr.mobile.payment.sdk.component.KeyProvider

/**
 * SDK configuration options class.
 *
 * @param keyProvider the encryption key provider to use.
 * @param cardInfoProvider the card style and type information provider to use.
 */
data class SDKConfig(
    val keyProvider: KeyProvider,
    val cardInfoProvider: CardInfoProvider,
)
