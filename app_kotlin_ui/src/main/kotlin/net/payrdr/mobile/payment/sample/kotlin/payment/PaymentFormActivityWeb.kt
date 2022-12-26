package net.payrdr.mobile.payment.sample.kotlin.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_payment_form.googlePayButtonFirst
import kotlinx.android.synthetic.main.activity_payment_form.mdOrder
import kotlinx.android.synthetic.main.activity_payment_form.paymentCheckout
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sample.kotlin.helpers.log
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.exceptions.SDKAlreadyPaymentException
import net.payrdr.mobile.payment.sdk.exceptions.SDKCryptogramException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKNotConfigureException
import net.payrdr.mobile.payment.sdk.exceptions.SDKOrderNotExistException
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.exceptions.SDKTransactionException
import net.payrdr.mobile.payment.sdk.form.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayUtils
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig

class PaymentFormActivityWeb : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_form)

        val baseUrl = "https://dev.bpcbt.com/payment"
        /* spellchecker: disable */
        val dsRoot = """
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

        val paymentConfig = SDKPaymentConfig(baseUrl, dsRoot,
            keyProviderUrl = "https://dev.bpcbt.com/payment/se/keys.do",
        )
        SDKPayment.init(paymentConfig, use3ds2sdk = false)
        paymentCheckout.setOnClickListener {
            SDKPayment.checkout(this, mdOrder.text.trim().toString())
        }
        googlePayButtonFirst.setOnClickListener {
            SDKPayment.checkout(this, mdOrder.text.trim().toString(), true)
        }

        GooglePayUtils.possiblyShowGooglePayButton(
            context = this,
            isReadyToPayJson = GooglePayUtils.getIsReadyToPayJson(),
            paymentsClient = GooglePayUtils.createPaymentsClient(
                this,
                GooglePayUtils.getEnvironment(isTest = true)
            ),
            callback = object : GooglePayUtils.GooglePayCheckCallback {
                override fun onNoGooglePlayServices() {
                    displayGooglePayButton(false)
                }

                override fun onNotReadyToRequest() {
                    displayGooglePayButton(false)
                }

                override fun onReadyToRequest() {
                    displayGooglePayButton(true)
                    googlePayButtonFirst.setOnClickListener {
                        SDKPayment.checkout(
                            this@PaymentFormActivityWeb,
                            mdOrder.text.trim().toString(),
                            true
                        )
                    }
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            makeToast("Canceled payment by user")
        }

        SDKPayment.handleCheckoutResult(requestCode, data, object :
            ResultPaymentCallback<PaymentData> {
            override fun onSuccess(result: PaymentData) {
                // Order payment result.
                log("PaymentData(${result.mdOrder}, ${result.status})")
            }

            override fun onFail(e: SDKException) {
                // An error occurred.
                when (e) {
                    is SDKAlreadyPaymentException -> makeToast(ERROR_ALREADY_DEPOSITED_ORDER)
                    is SDKCryptogramException -> makeToast(ERROR_CRYPTOGRAM_CANCELED)
                    is SDKDeclinedException -> makeToast(ERROR_DECLINED_ORDER)
                    is SDKPaymentApiException -> makeToast(ERROR_PAYMENT_API)
                    is SDKTransactionException -> makeToast(ERROR_WORK_CREATE_TRANSACTION)
                    is SDKOrderNotExistException -> makeToast(ERROR_ORDER_NOT_EXIT_API)
                    is SDKNotConfigureException -> makeToast(ERROR_NOT_CONFIGURE_EXCEPTION)
                    else -> log("${e.message} ${e.cause}")
                }
            }
        })
    }

    private fun makeToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun displayGooglePayButton(value: Boolean) {
        googlePayButtonFirst.visibility = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    companion object {
        private const val ERROR_WORK_CREATE_TRANSACTION: String =
            "Exception: while create transaction with EC or RSA"
        private const val ERROR_ALREADY_DEPOSITED_ORDER: String =
            "Exception: the order has already been deposited"
        private const val ERROR_DECLINED_ORDER: String = "Exception: the order has been declined"
        private const val ERROR_CRYPTOGRAM_CANCELED: String =
            "Exception: the cryptogram creation has been canceled or some error"
        private const val ERROR_PAYMENT_API: String = "Exception: the api work problem"
        private const val ERROR_ORDER_NOT_EXIT_API: String = "Exception: the order not exist"
        private const val ERROR_NOT_CONFIGURE_EXCEPTION: String =
            "Merchant is not configured to be used without 3DS2SDK"
    }
}