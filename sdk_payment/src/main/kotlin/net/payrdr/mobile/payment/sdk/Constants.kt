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
    internal const val REQUEST_CODE_3DS2_WEB = 60004
    internal const val INTENT_EXTRA_RESULT = "payment.sdk.result"
    internal const val TAG = "SDK Payment"
    internal const val INTENT_EXTRA_ACS_URL = "acsUrl"
    internal const val INTENT_EXTRA_TERM_URL = "termUrl"
    internal const val INTENT_EXTRA_PAREQ = "paReq"
}
