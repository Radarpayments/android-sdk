package net.payrdr.mobile.payment.sdk.payment

import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeParameters
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.Factory
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction

/**
 *  Interface describing the work of the 3DS form.
 */
interface ThreeDSFormDelegate {

    /**
     *  Service initialization .
     *
     *  @param threeDS2Service service object.
     *  @param factory class for managing UI component.
     */
    fun initThreeDS2Service(
        threeDS2Service: ThreeDS2Service,
        factory: Factory
    )

    /**
     *  Start Challenge Flow screen.
     *
     *  @param transaction transaction object.
     *  @param challengeParameters parameters for Challenge Flow.
     *  @param challengeStatusReceiver callback for Challenge Flow process.
     */
    fun openChallengeFlow(
        transaction: Transaction?,
        challengeParameters: ChallengeParameters,
        challengeStatusReceiver: ChallengeStatusReceiver
    )

    /**
     *  Start Web Challenge screen.
     *
     *  @param webChallengeParam parameters for Web Challenge.
     */
    fun openWebChallenge(
        webChallengeParam: WebChallengeParam,
    )

    /**
     *  Stop transaction and delete data.
     *
     *  @param transaction transaction object.
     *  @param threeDS2Service service object.
     */
    fun cleanup(
        transaction: Transaction?,
        threeDS2Service: ThreeDS2Service
    )
}
