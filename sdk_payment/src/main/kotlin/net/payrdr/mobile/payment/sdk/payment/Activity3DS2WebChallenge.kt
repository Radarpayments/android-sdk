package net.payrdr.mobile.payment.sdk.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import net.payrdr.mobile.payment.sdk.Constants
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.logs.Logger
import net.payrdr.mobile.payment.sdk.logs.Source
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
        @Suppress("LongMethod")
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            var isTrusted = false
            var message = ""
            when (error?.primaryError) {
                SslError.SSL_UNTRUSTED -> {
                    Logger.error(
                        this.javaClass,
                        Constants.TAG,
                        "onReceivedSslError $error",
                        Exception("SSL_UNTRUSTED"),
                        source = Source.WEB_VIEW
                    )
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
                            Logger.info(
                                this.javaClass,
                                Constants.TAG,
                                "onReceivedSslError $error",
                                null,
                                source = Source.WEB_VIEW
                            )
                        } catch (e: CertificateException) {
                            Logger.error(
                                this.javaClass,
                                Constants.TAG,
                                "onReceivedSslError $error",
                                Exception("WebClient onReceivedSslError - get Exception to try check server trusted."),
                                source = Source.WEB_VIEW
                            )
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
                        Logger.error(
                            this.javaClass,
                            Constants.TAG,
                            "onReceivedSslError $error",
                            Exception("Ssl context config must be not null when SSL_UNTRUSTED"),
                            source = Source.WEB_VIEW
                        )
                    }
                }

                SslError.SSL_EXPIRED -> {
                    message = "The certificate has expired."
                    Logger.error(
                        this.javaClass,
                        Constants.TAG,
                        "onReceivedSslError $error",
                        Exception("The certificate has expired"),
                        source = Source.WEB_VIEW
                    )
                }

                SslError.SSL_IDMISMATCH -> {
                    message = "The certificate Hostname mismatch."
                    Logger.error(
                        this.javaClass,
                        Constants.TAG,
                        "onReceivedSslError $error",
                        Exception("The certificate Hostname mismatch"),
                        source = Source.WEB_VIEW
                    )
                }

                SslError.SSL_NOTYETVALID -> {
                    message = "The certificate is not yet valid."
                    Logger.error(
                        this.javaClass,
                        Constants.TAG,
                        "onReceivedSslError $error",
                        Exception("The certificate is not yet valid"),
                        source = Source.WEB_VIEW
                    )
                }
            }
            LogDebug.logIfDebug("WebClient onReceivedSslError - $message")
            if (isTrusted) {
                handler?.proceed()
            } else {
                handler?.cancel()
            }
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            Logger.info(
                this.javaClass,
                Constants.TAG,
                "Intercepted Request: ${request}",
                null,
                source = Source.WEB_VIEW
            )
            return super.shouldInterceptRequest(view, request)
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            Logger.error(
                this.javaClass,
                Constants.TAG,
                "onReceivedError in WebView: $failingUrl}",
                Exception("Web Source Error with code: $errorCode"),
                source = Source.WEB_VIEW
            )
            super.onReceivedError(view, errorCode, description, failingUrl)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Logger.error(
                this.javaClass,
                Constants.TAG,
                "onReceivedError in WebView: $${request?.url}",
                Exception("Web Source Error with code: $error"),
                source = Source.WEB_VIEW
            )
            super.onReceivedError(view, request, error)
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
                                mdOrder = mdOrder,
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

    private val webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            when (consoleMessage?.messageLevel()) {
                ConsoleMessage.MessageLevel.TIP -> Logger.info(
                    this.javaClass,
                    Constants.TAG,
                    consoleMessage.message(),
                    null,
                    Source.WEB_VIEW
                )

                ConsoleMessage.MessageLevel.LOG -> Logger.info(
                    this.javaClass,
                    Constants.TAG,
                    consoleMessage.message(),
                    null,
                    Source.WEB_VIEW
                )

                ConsoleMessage.MessageLevel.WARNING -> Logger.warning(
                    this.javaClass,
                    Constants.TAG,
                    consoleMessage.message(),
                    null,
                    Source.WEB_VIEW
                )

                ConsoleMessage.MessageLevel.ERROR -> Logger.error(
                    this.javaClass,
                    Constants.TAG,
                    consoleMessage.message(),
                    Exception("Something went wrong in WebView: ${consoleMessage.message()}"),
                    Source.WEB_VIEW
                )

                ConsoleMessage.MessageLevel.DEBUG -> Logger.debug(
                    this.javaClass,
                    Constants.TAG,
                    consoleMessage.message(),
                    null,
                    Source.WEB_VIEW
                )

                null -> {}
            }
            return super.onConsoleMessage(consoleMessage)
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
        webView.webChromeClient = webChromeClient
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
                    mdOrder = mdOrder,
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
