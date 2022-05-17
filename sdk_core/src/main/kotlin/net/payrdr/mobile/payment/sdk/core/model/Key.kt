package net.payrdr.mobile.payment.sdk.core.model

/**
 * Data class key description for performing encryption of payment data.
 *
 * @param value key value.
 * @param protocol protocol.
 * @param expiration expiration time.
 */
data class Key(
    val value: String,
    val protocol: String,
    val expiration: Long
)
