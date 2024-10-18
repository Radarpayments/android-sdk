package net.payrdr.mobile.payment.sdk.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import net.payrdr.mobile.payment.sdk.Constants
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import java.security.cert.CertificateException
import java.util.Timer
import java.util.TimerTask
import javax.net.ssl.X509TrustManager

/**
 *  Activity for web challenge.
 */
class Activity3DS2WebChallenge : AppCompatActivity() {

    private lateinit var mdOrder: String
    private lateinit var timer: Timer

    /**
     * web view client for interception loading url.
     *
     */
    private val webViewClient: WebViewClient = object : WebViewClient() {

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            var isTrusted = false
            var message = ""
            when (error?.primaryError) {
                SslError.SSL_UNTRUSTED -> {
                    message = "The certificate authority is not trusted."
                    val sslContextConfig = SDKPayment.sdkPaymentConfig.sslContextConfig
                    if (sslContextConfig != null) {
                        try {
                            val trust = sslContextConfig.trustManager as X509TrustManager
                            trust.checkServerTrusted(
                                arrayOf(SDKPayment.sdkPaymentConfig.sslContextConfig?.customCertificate),
                                "RSA"
                            )
                            isTrusted = true
                            message = "The certificate authority is trusted."
                        } catch (e: CertificateException) {
                            LogDebug.logIfDebug(
                                "WebClient onReceivedSslError - get Exception to try check server trusted."
                            )
                        } catch (e: ClassCastException) {
                            LogDebug.logIfDebug(
                                "Cant cast Trust Manager to X509 Trust Manager."
                            )
                        }
                    } else {
                        message = "Ssl context config must be not null when SSL_UNTRUSTED"
                    }
                }

                SslError.SSL_EXPIRED -> {
                    message = "The certificate has expired."
                }

                SslError.SSL_IDMISMATCH -> {
                    message = "The certificate Hostname mismatch."
                }

                SslError.SSL_NOTYETVALID -> {
                    message = "The certificate is not yet valid."
                }
            }
            LogDebug.logIfDebug("WebClient onReceivedSslError - $message")
            if (isTrusted) {
                handler?.proceed()
            } else {
                handler?.cancel()
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request != null) {
                when {
                    request.url.toString().startsWith("sdk://done") -> {
                        finishWithResult(
                            PaymentResult(
                                sessionId = mdOrder,
                                isSuccess = true,
                                exception = null,
                            )
                        )
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
        } catch (ex: Exception) {
            finish()
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

    override fun onDestroy() {
        timer.cancel()
        timer.purge()
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
        ): Intent = Intent(context, Activity3DS2WebChallenge::class.java).apply {
            putExtra(Constants.MDORDER, webChallengeParam.mdOrder)
            putExtra(Constants.INTENT_EXTRA_ACS_URL, webChallengeParam.acsUrl)
            putExtra(Constants.INTENT_EXTRA_PAREQ, webChallengeParam.paReq)
            putExtra(Constants.INTENT_EXTRA_TERM_URL, webChallengeParam.termUrl)
        }
    }

    private inner class WebChallengeTimeoutTask : TimerTask() {
        override fun run() {
            finishWithResult(
                PaymentResult(
                    sessionId = mdOrder,
                    isSuccess = false,
                    exception = SDKException(message = "Transaction Timed Out."),
                )
            )
        }
    }

    /**
     * Terminates the [Activity] on which this method was called with passing as a result
     * [Activity] work with value [paymentData].
     *
     * @param paymentData - result of [Activity] work.
     */
    private fun finishWithResult(paymentData: PaymentResult) {
        val resultIntent = Intent().apply {
            putExtra(Constants.INTENT_EXTRA_RESULT, paymentData)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
