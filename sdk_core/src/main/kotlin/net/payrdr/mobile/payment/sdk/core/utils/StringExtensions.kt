package net.payrdr.mobile.payment.sdk.core.utils

import android.graphics.Color
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import java.util.Calendar
import java.util.Date

/**
 * Return content of pem file by line.
 *
 * @return content of pem file.
 */
fun String.pemKeyContent(): String =
    replace("\\s+", "")
        .replace("\n", "")
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")

/**
 * Extension for getting color from string format  #ffffff or abbreviated form #fff.
 *
 * @return the color obtained from the string representation .
 */
@Suppress("MagicNumber")
fun String.parseColor(): Int = Color.parseColor(
    if (this.length == 4) {
        "#${this[1]}${this[1]}${this[2]}${this[2]}${this[3]}${this[3]}"
    } else {
        this
    }
)

/**
 * Extension for getting a term consisting only of numbers, all other characters will be removed.
 *
 * If the [maxLength] parameter is specified, then the string of digits will be truncated to the specified length.
 *
 * @param maxLength max length of line.
 * @return processed line.
 */
fun String.digitsOnly(maxLength: Int? = null) = run {
    val digits = replace("[^\\d.]".toRegex(), "")
    if (maxLength != null) {
        digits.take(maxLength)
    } else {
        digits
    }
}

/**
 * Extension to get timeline without spaces, all spaces will be removed.
 *
 * If the [maxLength] parameter is specified, then the string without spaces will be truncated to the specified length.
 *
 * @param maxLength max length of line.
 * @return processed line.
 */
fun String.noSpaces(maxLength: Int? = null) = run {
    val digits = replace("[\\s.]".toRegex(), "")
    if (maxLength != null) {
        digits.take(maxLength)
    } else {
        digits
    }
}

/**
 * Returns an [ExpiryDate] object built on a MM/YY format string.
 *
 * @return information about the validity of the card.
 */
@Suppress("MagicNumber")
fun String.toExpDate(): ExpiryDate {
    if (!matches("\\d{2}/\\d{2}".toRegex())) {
        throw IllegalArgumentException("Incorrect format, should be MM/YY.")
    }
    return ExpiryDate(
        expMonth = substring(0, 2).toInt(),
        expYear = substring(3, 5).toInt() + 2000
    )
}

/**
 * Converts a date to an MM/YY format term.
 *
 * @return information about the validity of the card.
 */
@Suppress("MagicNumber")
fun Date.toStringExpDate(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this
    val month = (calendar.get(Calendar.MONTH) + 1).toString()
        .padStart(2, '0')
    val year = calendar.get(Calendar.YEAR) % 100
    return "$month/$year"
}
