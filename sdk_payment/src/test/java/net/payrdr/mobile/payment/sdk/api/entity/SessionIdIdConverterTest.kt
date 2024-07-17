package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import net.payrdr.mobile.payment.sdk.exceptions.SDKBadSessionIdException
import net.payrdr.mobile.payment.sdk.utils.SessionIdConverter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.assertThrows

@SmallTest
class SessionIdIdConverterTest {

    @Test
    fun shouldCorrectConvertSessionIdToMdOrder() {
        val sessionId = "ps_2GRSj4LvzJK36nesSKyURHRdbCiztLZowGhHmmuXDe4LdQPk2igBHb"
        val mdOrder = SessionIdConverter.sessionIdToMdOrder(sessionId)
        val actual = "b8dd201e-5da0-4655-bc31-c3470698c172"
        assertEquals(mdOrder, actual)
    }

    @Test
    fun shouldCorrectConvertMdOrderToSessionId() {
        val mdOrder = "b8dd201e-5da0-4655-bc31-c3470698c172"
        val sessionId = SessionIdConverter.mdOrderToSessionId(mdOrder)
        val actual = "ps_2GRSj4LvzJK36nesSKyURHRdbCiztLZowGhHmmuXDe4LdQPk2igBHb"
        assertEquals(sessionId, actual)
    }

    @Test
    fun shouldThrowExceptionWhenEmptySessionId() {
        val sessionId = ""
        val exception = assertThrows<SDKBadSessionIdException> {
            SessionIdConverter.sessionIdToMdOrder(sessionId)
        }
        val actual = "sessionId must be not empty"
        assertEquals(exception.message, actual)
    }

    @Test
    fun shouldThrowExceptionWhenSessionIdNotStartWithPrefix() {
        val sessionId = "2GRSj4LvzJK36nesSKyURHRdbCiztLZowGhHmmuXDe4LdQPk2igBHb"
        val exception = assertThrows<SDKBadSessionIdException> {
            SessionIdConverter.sessionIdToMdOrder(sessionId)
        }
        val actual = "Incorrect sessionId: $sessionId"
        assertEquals(exception.message, actual)
    }

    @Test
    fun shouldThrowExceptionWhenSessionIdIsNotValidBase58() {
        val sessionId = "ps_2GRSj4LvzJK36nesSKyURHRdbCiztLZowiOolGhHmmuXDe4LdQPk2igBHb"
        val exception = assertThrows<SDKBadSessionIdException> {
            SessionIdConverter.sessionIdToMdOrder(sessionId)
        }
        val actual = "SessionId is not correct Base58 string: $sessionId"
        assertEquals(exception.message, actual)
    }

    @Test
    fun shouldThrowExceptionWhenNoInsidePrefix() {
        val sessionId = "ps_kFuWLtGpdLebiygMT25cjZyuAVfNqhHDeitzQPnGqNAhAHdUu"
        val exception = assertThrows<SDKBadSessionIdException> {
            SessionIdConverter.sessionIdToMdOrder(sessionId)
        }
        val actual = "SessionId is not correct: $sessionId"
        assertEquals(exception.message, actual)
    }
}
