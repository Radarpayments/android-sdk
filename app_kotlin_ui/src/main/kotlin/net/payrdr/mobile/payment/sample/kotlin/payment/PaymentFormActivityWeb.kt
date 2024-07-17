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
import net.payrdr.mobile.payment.sample.kotlin.MarketApplication
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sample.kotlin.helpers.log
import net.payrdr.mobile.payment.sdk.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayUtils
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.Use3DSConfig

class PaymentFormActivityWeb : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_form)

        val baseUrl = "https://dev.bpcbt.com/payment"
        val paymentConfig = SDKPaymentConfig(
            baseUrl,
            use3DSConfig = Use3DSConfig.NoUse3ds2sdk,
            sslContextConfig = MarketApplication.sslContextConfig,
        )
        SDKPayment.init(paymentConfig)
        paymentCheckout.setOnClickListener {
            SDKPayment.checkout(this, CheckoutConfig.MdOrder(mdOrder.text.trim().toString()))
        }
        googlePayButtonFirst.setOnClickListener {
            SDKPayment.checkout(this, CheckoutConfig.MdOrder(mdOrder.text.trim().toString()), true)
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
                            CheckoutConfig.MdOrder(mdOrder.text.trim().toString()),
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
            ResultPaymentCallback<PaymentResult> {
            override fun onResult(result: PaymentResult) {
                // Order payment result.
                log("PaymentData(${result.sessionId}, ${result.isSuccess}) ${result.exception}")
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

}