package net.payrdr.mobile.payment.sdk.payment

import android.content.Context
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeParameters
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction

/**
 *  Interface describing the work of the 3DS2 form.
 */
interface ThreeDS2FormDelegate {

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
     *  Stop transaction and delete data.
     *
     *  @param transaction transaction object.
     *  @param threeDS2Service service object.
     */
    fun cleanup(
        transaction: Transaction?,
        threeDS2Service: ThreeDS2Service
    )

    /**
     * Provides application context.
     *
     * @return application context.
     */
    fun getApplicationContext(): Context

    /**
     * Provides base context.
     *
     * @return base context.
     */
    fun getBaseContext(): Context

}
