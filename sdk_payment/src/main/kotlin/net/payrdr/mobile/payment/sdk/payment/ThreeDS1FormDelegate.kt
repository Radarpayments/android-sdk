package net.payrdr.mobile.payment.sdk.payment

import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam

/**
 *  Interface describing the work of the 3DS1 form.
 */
interface ThreeDS1FormDelegate {
    /**
     *  Start Web Challenge screen.
     *
     *  @param webChallengeParam parameters for Web Challenge.
     */
    fun openWebChallenge(
        webChallengeParam: WebChallengeParam,
    )

}
