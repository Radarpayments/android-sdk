package net.payrdr.mobile.payment.sdk.utils

import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate

private const val PAN_VISIBLE_DIGITS = 4
private const val EXPIRY_PART_LENGTH = 2
private const val EXPIRY_YEAR_DIVIDER = 100

/**
 * Masked stored card number for read-only display: "411111**1111" -> "4111 •••• •••• 1111".
 *
 * @return the first and last 4 characters of the number with the middle hidden.
 */
internal fun String.maskedPanForDisplay(): String =
    if (length < PAN_VISIBLE_DIGITS * 2) {
        this
    } else {
        "${take(PAN_VISIBLE_DIGITS)} •••• •••• ${takeLast(PAN_VISIBLE_DIGITS)}"
    }

/**
 * Card expiry for display in MM/YY format.
 *
 * @return expiry date, e.g. "12/26".
 */
internal fun ExpiryDate.toDisplayString(): String =
    expMonth.toString().padStart(EXPIRY_PART_LENGTH, '0') + "/" +
        (expYear % EXPIRY_YEAR_DIVIDER).toString().padStart(EXPIRY_PART_LENGTH, '0')
