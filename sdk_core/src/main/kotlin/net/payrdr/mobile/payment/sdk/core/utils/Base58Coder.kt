package net.payrdr.mobile.payment.sdk.core.utils

import java.math.BigInteger

/**
 * Utilities for encoding and decoding the Base58 representation of string.
 */
@Suppress("MagicNumber")
object Base58Coder {

    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val BASE = BigInteger.valueOf(ALPHABET.length.toLong())

    fun encode(input: String): String {
        val inputBytes = input.toByteArray()
        var num = BigInteger(1, inputBytes)
        val sb = StringBuilder()
        while (num > BigInteger.ZERO) {
            val remainder = num.mod(BASE)
            num = num.divide(BASE)
            sb.append(ALPHABET[remainder.toInt()])
        }
        // Deal with leading zeros
        for (byte in inputBytes) {
            if (byte.toInt() == 0) {
                sb.append(ALPHABET[0])
            } else {
                break
            }
        }
        return sb.reverse().toString()
    }

    fun decode(input: String): String {
        require(isValidBase58String(input)) { "Input is not a valid Base58 string." }
        var num = BigInteger.ZERO
        for (char in input) {
            num = num.multiply(BASE)
            num = num.add(BigInteger.valueOf(ALPHABET.indexOf(char).toLong()))
        }

        val decoded = num.toByteArray()

        // Handle leading zeros
        val leadingZeros = input.takeWhile { it == ALPHABET[0] }.count()
        val output = ByteArray(leadingZeros + decoded.size)
        System.arraycopy(decoded, 0, output, leadingZeros, decoded.size)

        return output.decodeToString()
    }

    fun isValidBase58String(input: String): Boolean {
        return input.all { ALPHABET.contains(it) }
    }

}
