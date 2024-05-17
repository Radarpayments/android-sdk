package net.payrdr.mobile.payment.sdk.payment

import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam

/**
 *  Interface describing the work of the 3DS2 web form.
 */
interface ThreeDS2WebFormDelegate {
    /**
     *  Start Web Challenge screen.
     *
     *  @param webChallengeParam parameters for Web Challenge.
     */
    fun openWebChallenge(
        webChallengeParam: WebChallengeParam,
    )

}
