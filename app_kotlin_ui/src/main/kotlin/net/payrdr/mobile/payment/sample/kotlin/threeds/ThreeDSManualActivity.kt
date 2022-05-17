package net.payrdr.mobile.payment.sample.kotlin.threeds

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.payrdr.mobile.payment.sdk.threeds.impl.Factory
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.CompletionEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ProtocolErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.RuntimeErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction
import kotlinx.android.synthetic.main.activity_three_d_s_manual.acsRefNumber
import kotlinx.android.synthetic.main.activity_three_d_s_manual.acsSignedContent
import kotlinx.android.synthetic.main.activity_three_d_s_manual.acsTransactionID
import kotlinx.android.synthetic.main.activity_three_d_s_manual.executeThreeDSChallengeFlow
import kotlinx.android.synthetic.main.activity_three_d_s_manual.initThreeDSTransaction
import kotlinx.android.synthetic.main.activity_three_d_s_manual.logView
import kotlinx.android.synthetic.main.activity_three_d_s_manual.threeDSAuthenticationRequestParams
import kotlinx.android.synthetic.main.activity_three_d_s_manual.threeDSServerTransId
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sample.kotlin.helpers.copyToClipboard
import net.payrdr.mobile.payment.sample.kotlin.helpers.launchGlobalScope
import net.payrdr.mobile.payment.sample.kotlin.helpers.launchMainGlobalScope
import net.payrdr.mobile.payment.sample.kotlin.helpers.log

class ThreeDSManualActivity : AppCompatActivity() {

    // The fields are required to create and run the 3DS Challenge Flow.
    private val factory = Factory()
    private lateinit var threeDS2Service: ThreeDS2Service
    private var transaction: Transaction? = null

    // Customizable parameters of the example from the UI.
    private val argAcsTransactionID: String
        get() = acsTransactionID.text.toString()
    private val argAcsRefNumber: String
        get() = acsRefNumber.text.toString()
    private val argAcsSignedContent: String
        get() = acsSignedContent.text.toString()
    private val argThreeDSServerTransId: String
        get() = threeDSServerTransId.text.toString()

    /* spellchecker: disable */
    private val dsRoot =
        """
        MIICDTCCAbOgAwIBAgIUOO3a573khC9kCsQJGKj/PpKOSl8wCgYIKoZIzj0EA
        wIwXDELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBA
        oMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDEVMBMGA1UEAwwMZHVtbXkzZHN
        yb290MB4XDTIxMDkxNDA2NDQ1OVoXDTMxMDkxMjA2NDQ1OVowXDELMAkGA1UE
        BhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0I
        FdpZGdpdHMgUHR5IEx0ZDEVMBMGA1UEAwwMZHVtbXkzZHNyb290MFkwEwYHKo
        ZIzj0CAQYIKoZIzj0DAQcDQgAE//e+MhwdgWxkFpexkjBCx8FtJ24KznHRXMS
        WabTrRYwdSZMScgwdpG1QvDO/ErTtW8IwouvDRlR2ViheGr02bqNTMFEwHQYD
        VR0OBBYEFHK/QzMXw3kW9UzY5w9LVOXr+6YpMB8GA1UdIwQYMBaAFHK/QzMXw
        3kW9UzY5w9LVOXr+6YpMA8GA1UdEwEB/wQFMAMBAf8wCgYIKoZIzj0EAwIDSA
        AwRQIhAOPEiotH3HJPIjlrj9/0m3BjlgvME0EhGn+pBzoX7Z3LAiAOtAFtkip
        d9T5c9qwFAqpjqwS9sSm5odIzk7ug8wow4Q==
        """
            /* spellchecker: enable */
            .replace("\n", "")
            .trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_d_s_manual)
        threeDS2Service = factory.newThreeDS2Service()
        val configParams = factory.newConfigParameters()
        val uiCustomization = factory.newUiCustomization(baseContext)
        threeDS2Service.initialize(
            this@ThreeDSManualActivity,
            configParams,
            "en-US",
            uiCustomization
        )
        initThreeDSTransaction.setOnClickListener {
            initThreeDSChallengeFlow()
        }
        executeThreeDSChallengeFlow.setOnClickListener {
            executeThreeDSChallengeFlow()
        }
        logView.setOnClickListener {
            copyToClipboard("3DS logs", (it as TextView).text.toString())
        }
    }

    private fun initThreeDSChallengeFlow() = launchGlobalScope {
        cleanLogView()
        transaction?.close() // Close the previous transaction if there was one.
        transaction = threeDS2Service.createTransaction("F000000000", "", "2.1.0", dsRoot)

        // Available data, to be sent to the payment gateway.
        val authRequestParams = transaction!!.authenticationRequestParameters!!
        val encryptedDeviceInfo: String = authRequestParams.deviceData
        val sdkTransactionID: String = authRequestParams.sdkTransactionID
        val sdkAppId: String = authRequestParams.sdkAppID
        val sdkEphmeralPublicKey: String = authRequestParams.sdkEphemeralPublicKey
        val sdkReferenceNumber: String = authRequestParams.sdkReferenceNumber


        launchMainGlobalScope {
            threeDSAuthenticationRequestParams.setText(
                """
                    sdkTransactionID:$sdkTransactionID
                    sdkAppId:$sdkAppId
                    sdkEphmeralPublicKey:$sdkEphmeralPublicKey
                    sdkReferenceNumber:$sdkReferenceNumber
                    encryptedDeviceInfo:$encryptedDeviceInfo
                """.trimIndent()
            )
            copyToClipboard(
                "3DS authentication request parameters",
                threeDSAuthenticationRequestParams.text.toString()
            )
        }
    }

    private fun executeThreeDSChallengeFlow() = launchGlobalScope {
        if (transaction == null) {
            log("Init transaction before")
            return@launchGlobalScope
        }

        val challengeParameters = factory.newChallengeParameters()

        // Parameters for starting Challenge Flow.
        challengeParameters.acsTransactionID = argAcsTransactionID
        challengeParameters.acsRefNumber = argAcsRefNumber
        challengeParameters.acsSignedContent = argAcsSignedContent
        challengeParameters.set3DSServerTransactionID(argThreeDSServerTransId)

        // Listener to handle the Challenge Flow execution process.
        val challengeStatusReceiver: ChallengeStatusReceiver = object : ChallengeStatusReceiver {
            override fun cancelled() {
                log("cancelled")
                printLogView("cancelled")
                cleanup()
            }

            override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
                log("protocolError $protocolErrorEvent")
                printLogView("protocolError $protocolErrorEvent")
                cleanup()
            }

            override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
                log("runtimeError $runtimeErrorEvent")
                printLogView("runtimeError $runtimeErrorEvent")
                cleanup()
            }

            override fun completed(completionEvent: CompletionEvent) {
                log("completed $completionEvent")
                printLogView("completed $completionEvent")
                cleanup()
            }

            override fun timedout() {
                log("timedout")
                printLogView("timedout")
                cleanup()
            }
        }
        val timeOut = 5

        // Starting Challenge Flow.
        transaction!!.doChallenge(
            this@ThreeDSManualActivity,
            challengeParameters,
            challengeStatusReceiver,
            timeOut
        )
    }

    private fun cleanLogView() {
        runOnUiThread {
            logView.text = null
        }
    }

    private fun printLogView(message: String) {
        runOnUiThread {
            logView.append("\n$message")
        }
    }

    override fun onDestroy() {
        threeDS2Service.cleanup(this@ThreeDSManualActivity)
        super.onDestroy()
    }

    private fun cleanup() {
        transaction?.close()
    }
}