package net.payrdr.mobile.payment.sdk.data

internal data class TestCard(
    val pan: String,
    val expiry: String,
    val cvc: String,
    val holder: String,
)
