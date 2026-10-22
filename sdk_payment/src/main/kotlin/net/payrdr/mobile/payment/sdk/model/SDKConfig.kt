package net.payrdr.mobile.payment.sdk.model

import net.payrdr.mobile.payment.sdk.component.KeyProvider

/**
 * SDK configuration options class.
 *
 * @param keyProvider the encryption key provider to use.
 */
data class SDKConfig(
    val keyProvider: KeyProvider,
)
