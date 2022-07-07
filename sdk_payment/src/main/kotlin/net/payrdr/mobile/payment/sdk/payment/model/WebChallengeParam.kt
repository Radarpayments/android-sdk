package net.payrdr.mobile.payment.sdk.payment.model

/**
 * Web Challenge params class .
 *
 * @param mdOrder order number.
 * @param acsUrl automatic configuration server url.
 * @param paReq params request.
 * @param termUrl terminal url.
 */
data class WebChallengeParam(
    val mdOrder: String,
    val acsUrl: String,
    val paReq: String,
    val termUrl: String,
)
