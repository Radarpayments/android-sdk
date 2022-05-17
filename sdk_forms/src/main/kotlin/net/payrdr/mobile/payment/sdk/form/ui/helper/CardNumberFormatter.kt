package net.payrdr.mobile.payment.sdk.form.ui.helper

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.form.R

internal object CardNumberFormatter {

    fun maskCardNumber(context: Context, pan: String): String {
        val clearPan = pan.digitsOnly()
        val number = when {
            clearPan.length >= PAN_LAST_DIGITS_COUNT -> clearPan.takeLast(PAN_LAST_DIGITS_COUNT)
            else -> pan
        }
        return context.resources.getString(R.string.payrdr_card_list_pan_pattern, number)
    }

    private const val PAN_LAST_DIGITS_COUNT = 4
}
