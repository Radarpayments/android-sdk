package net.payrdr.mobile.payment.sdk.ui.helper

/**
 * Card network and the name of its logo in `assets/payment_system`, null when there is no logo.
 */
internal enum class CardNetwork(val logoName: String?) {
    VISA("visa"),
    MASTERCARD("mastercard"),
    AMEX("amex"),
    UNIONPAY("cup"),
    DINERS("dinersclub"),
    JCB("jcb"),
    DISCOVER("discover"),
    MAESTRO("maestro"),
    UNKNOWN(null),
}

/**
 * Resolves a partial or complete card number to its [CardNetwork] by the leading digits.
 * Same ranges as CardNetworkResolver in the KMP SDK. Mir (2200–2204) is deliberately not recognised.
 */
internal object CardNetworkResolver {

    private val NETWORK_PREFIXES: List<Pair<CardNetwork, List<ClosedRange<String>>>> = listOf(
        CardNetwork.VISA to listOf("4".."4"),
        CardNetwork.AMEX to listOf("34".."34", "37".."37"),
        CardNetwork.MASTERCARD to listOf("51".."55", "2221".."2720"),
        CardNetwork.UNIONPAY to listOf("62".."62"),
        CardNetwork.DINERS to listOf("36".."36", "38".."39", "300".."305", "3095".."3095"),
        CardNetwork.JCB to listOf("3528".."3589"),
        CardNetwork.DISCOVER to listOf("6011".."6011", "644".."649", "65".."65"),
        CardNetwork.MAESTRO to listOf("50".."50", "56".."58", "6304".."6304", "6759".."6759", "6761".."6763"),
    )

    fun resolve(cardNumber: String): CardNetwork {
        val digits = cardNumber.filter { it.isDigit() }
        return NETWORK_PREFIXES
            .firstOrNull { (_, prefixes) -> prefixes.any { digits.startsWithIn(it) } }
            ?.first
            ?: CardNetwork.UNKNOWN
    }

    private fun String.startsWithIn(prefixes: ClosedRange<String>): Boolean {
        val length = prefixes.start.length
        return this.length >= length && take(length) in prefixes
    }
}
