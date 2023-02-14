package net.payrdr.mobile.payment.sdk.form.model

import net.payrdr.mobile.payment.sdk.form.component.CardInfoProvider
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider

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
