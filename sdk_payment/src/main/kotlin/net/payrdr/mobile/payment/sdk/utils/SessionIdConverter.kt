package net.payrdr.mobile.payment.sdk.utils

import net.payrdr.mobile.payment.sdk.core.utils.Base58Coder
import net.payrdr.mobile.payment.sdk.exceptions.SDKBadSessionIdException

private const val OUTSIDE_BASE58_PREFIX = "ps_"
private const val INSIDE_BASE58_PREFIX = "ps:"

/**
 * Helper class to convert session id to order number and back.
 *
 */

@Suppress("ThrowsCount")
object SessionIdConverter {

    fun mdOrderToSessionId(mdOrder: String): String {
        return buildSessionIdFromMdOrder(mdOrder)
    }

    fun sessionIdToMdOrder(sessionId: String): String {
        return parseMdOrderFromApiV2Id(sessionId)
    }

    private fun parseMdOrderFromApiV2Id(apiV2Id: String): String {
        if (apiV2Id.isBlank()) {
            throw SDKBadSessionIdException(
                message = "sessionId must be not empty",
                cause = IllegalArgumentException()
            )
        }
        if (apiV2Id.startsWith(OUTSIDE_BASE58_PREFIX).not()) {
            throw SDKBadSessionIdException(
                message = "Incorrect sessionId: $apiV2Id",
                cause = IllegalArgumentException()
            )
        }
        val base58Id = apiV2Id.substring(OUTSIDE_BASE58_PREFIX.length)
        if (Base58Coder.isValidBase58String(base58Id).not()) {
            throw SDKBadSessionIdException(
                message = "SessionId is not correct Base58 string: $apiV2Id",
                cause = IllegalArgumentException()
            )
        }
        val decoded: String = Base58Coder.decode(base58Id)
        if (decoded.startsWith(INSIDE_BASE58_PREFIX).not()) {
            throw SDKBadSessionIdException(
                message = "SessionId is not correct: $apiV2Id",
                cause = IllegalArgumentException()
            )
        }
        val result: String = decoded.substring(OUTSIDE_BASE58_PREFIX.length)
        return result
    }

    private fun buildSessionIdFromMdOrder(mdOrder: String): String {
        val encoded = Base58Coder.encode((INSIDE_BASE58_PREFIX + mdOrder))
        val result: String = OUTSIDE_BASE58_PREFIX + encoded.trim()
        return result
    }
}
