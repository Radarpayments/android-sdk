package net.payrdr.mobile.payment.sdk

/**
 * Global constants used in the Payment SDK.
 */
object Constants {
    internal const val MDORDER = "mdOrder"
    internal const val IS_GOOGLE_PAY = "isGooglePay"
    internal const val TIMEOUT_THREE_DS = 5
    internal const val REQUEST_CODE_CRYPTOGRAM = 60000
    internal const val REQUEST_CODE_PAYMENT = 60003
    internal const val INTENT_EXTRA_RESULT = "payment.sdk.result"
    internal const val INTENT_EXTRA_ERROR = "payment.sdk.error"
}
