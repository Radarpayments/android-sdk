package net.payrdr.mobile.payment.sdk.core.utils

private const val PREFIX = "pm_"

/**
 * Utilities for parsing payment method id.
 */

object BindingUtils {

    fun extractBindingId(paymentMethodId: String): String {
        if (isValidPaymentMethodId(paymentMethodId).not()) {
            return ""
        }
        val body = paymentMethodId.substring(PREFIX.length)
        val bindingId = Base58Coder.decode(body).trim()
        return bindingId
    }

    private fun isValidPaymentMethodId(paymentMethodId: String): Boolean {
        if (paymentMethodId.startsWith(PREFIX).not()) {
            return false
        }
        val body = paymentMethodId.substring(PREFIX.length)
        return Base58Coder.isValidBase58String(body)
    }
}
