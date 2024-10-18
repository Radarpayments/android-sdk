package net.payrdr.mobile.payment.sdk.core.utils

import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.assertThrows

@SmallTest
class Base58CoderTest {

    @Test
    fun shouldEncode58Correct() {
        val input = "ps:b8dd201e-5da0-4655-bc31-c3470698c172"
        val output = Base58Coder.encode(input)
        val correct = "2GRSj4LvzJK36nesSKyURHRdbCiztLZowGhHmmuXDe4LdQPk2igBHb"
        assertEquals(output, correct)
    }

    @Test
    fun shouldDecode58Correct() {
        val input = "mDAfQP9P4A4mMrFUdWsexjp53c1wLMzY6Pkt4j99nAXEWRWib"
        val output = Base58Coder.decode(input)
        val correct = "db9eef3a-2281-4c7e-92ee-194da62fbce0"
        assertEquals(output, correct)
    }

    @Test
    fun shouldEncodeCorrectlyEmptyString() {
        val input = ""
        val output = Base58Coder.encode(input)
        val correct = ""
        assertEquals(output, correct)
    }

    @Test
    fun shouldThrowExceptionWhenDecodeIncorrectString() {
        val input = "IsdgkslhlhghO"
        assertThrows<IllegalArgumentException> {
            Base58Coder.decode(input)
        }
    }

    @Test
    fun shouldReturnFalseWithInvalidBase58String() {
        val input = "ps_b8dd201e-5da0-4655-bc31-c3470698c172"
        val output = Base58Coder.isValidBase58String(input)
        assertEquals(output, false)
    }
}
