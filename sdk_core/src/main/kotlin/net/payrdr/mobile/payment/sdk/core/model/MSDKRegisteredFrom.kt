package net.payrdr.mobile.payment.sdk.core.model

/**
 * @param registeredFromValue token generation source.
 */
enum class MSDKRegisteredFrom(val registeredFromValue: String) {
    MSDK_CORE("MSDK_CORE"),
    MSDK_FORMS("MSDK_FORMS"),
    MSDK_PAYMENT("MSDK_PAYMENT"),
}
