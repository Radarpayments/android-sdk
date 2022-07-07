package net.payrdr.mobile.payment.sdk.payment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.Constants
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.PaymentApiImpl
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.utils.finishWithError
import net.payrdr.mobile.payment.sdk.utils.finishWithResult
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeoutException

/**
 *  Activity for web challenge.
 */
class ActivityWebChallenge : AppCompatActivity() {

    private lateinit var mdOrder: String
    private lateinit var timer: Timer
    private val paymentScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val sdkPaymentConfig: SDKPaymentConfig = SDKPayment.sdkPaymentConfig
    private val paymentApi: PaymentApi = PaymentApiImpl(
        baseUrl = sdkPaymentConfig.baseURL
    )

    /**
     * web view client for interception loading url.
     *
     */
    private val webViewClient: WebViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request != null) {
                when {
                    request.url.toString().contains("sdk://done") -> {
                        finishActivityWithStatus()
                    }
                    else -> {
                        view?.loadUrl(request.url.toString())
                    }
                }
            }
            return true
        }
    }

    /**
     * Configure html load data web view.
     *
     * @param mdOrder order number.
     * @param acsUrl automatic configuration server url.
     * @param paReq params request.
     * @param termUrl terminal url.
     */
    private fun getDataHTML(
        mdOrder: String,
        acsUrl: String,
        paReq: String,
        termUrl: String
    ): String =
        """
        <html>
        <head><title>ACS Redirect</title></head>
        <body onload="document.forms['acs'].submit()">
        ACS Redirect
        <form id="acs" method="post" action="$acsUrl">
            <input type="hidden" id="MD" name="MD" value="$mdOrder"/>
            <input type="hidden" id="PaReq" name="PaReq" value="$paReq"/>
            <input type="hidden" id="TermUrl" name="TermUrl" value="$termUrl"/>
        </form>
        </body>
        </html>
        """.trimIndent()

    @Suppress("TooGenericExceptionCaught")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_challenge)
        timer = Timer()
        timer.schedule(WebChallengeTimeoutTask(), TIME_OUT)

        try {
            mdOrder = requireNotNull(intent.getStringExtra(Constants.MDORDER))
            val acsUrl = requireNotNull(intent.getStringExtra(Constants.INTENT_EXTRA_ACS_URL))
            val paReq = requireNotNull(intent.getStringExtra(Constants.INTENT_EXTRA_PAREQ))
            val termUrl = requireNotNull(intent.getStringExtra(Constants.INTENT_EXTRA_TERM_URL))

            val webView = configureWebView()
            val loadData = getDataHTML(mdOrder, acsUrl, paReq, termUrl)

            webView.loadData(loadData, "text/html", "UTF-8")
        } catch (nullPointerException: NullPointerException) {
            finishWithError(SDKPaymentApiException(cause = nullPointerException))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(): WebView {
        val webView = findViewById<WebView>(R.id.web_view)
        webView.apply {
            with(settings) {
                javaScriptCanOpenWindowsAutomatically = true
                javaScriptEnabled = true
                domStorageEnabled = true
            }
        }
        WebView.setWebContentsDebuggingEnabled(true)
        webView.webViewClient = webViewClient
        return webView
    }

    private fun finishActivityWithStatus() {
        paymentScope.launch {
            val orderStatus = paymentApi.getSessionStatus(mdOrder = mdOrder)
            val paymentFinishedInfo = paymentApi.getFinishedPaymentInfo(mdOrder)
            val paymentDataResponse =
                PaymentData(mdOrder = mdOrder, status = paymentFinishedInfo.status)

            LogDebug.logIfDebug("getSessionStatus - Remaining sec ${orderStatus.remainingSecs}")

            finishWithResult(paymentDataResponse)
        }
    }

    override fun onDestroy() {
        timer.cancel()
        timer.purge()
        paymentScope.cancel()
        super.onDestroy()
    }

    companion object {

        private const val TIME_OUT = 300_000L

        /**
         * Prepares the [Intent] to start the Web Challenge screen.
         *
         * @param context to prepare intent.
         * @param webChallengeParam parameters for Web Challenge.
         */
        fun prepareIntent(
            context: Context,
            webChallengeParam: WebChallengeParam,
        ): Intent = Intent(context, ActivityWebChallenge::class.java).apply {
            putExtra(Constants.MDORDER, webChallengeParam.mdOrder)
            putExtra(Constants.INTENT_EXTRA_ACS_URL, webChallengeParam.acsUrl)
            putExtra(Constants.INTENT_EXTRA_PAREQ, webChallengeParam.paReq)
            putExtra(Constants.INTENT_EXTRA_TERM_URL, webChallengeParam.termUrl)
        }
    }

    private inner class WebChallengeTimeoutTask : TimerTask() {
        override fun run() {
            finishWithError(TimeoutException("Transaction Timed Out."))
        }
    }
}
