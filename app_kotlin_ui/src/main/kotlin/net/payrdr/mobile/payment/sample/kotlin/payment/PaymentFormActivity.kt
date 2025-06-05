package net.payrdr.mobile.payment.sample.kotlin.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_payment_form.mdOrder
import kotlinx.android.synthetic.main.activity_payment_form.paymentCheckout
import net.payrdr.mobile.payment.sample.kotlin.MarketApplication
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sample.kotlin.helpers.log
import net.payrdr.mobile.payment.sdk.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig

class PaymentFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_form)

        val baseUrl = "https://dev.bpcbt.com/payment"
        val paymentConfig = SDKPaymentConfig(
            baseUrl,
            sslContextConfig = MarketApplication.sslContextConfig,
        )
        SDKPayment.init(paymentConfig)
        paymentCheckout.setOnClickListener {
            SDKPayment.checkout(this, CheckoutConfig.MdOrder(mdOrder.text.trim().toString()))
        }
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
                log("PaymentData(${result.sessionId}, ${result.isSuccess} {${result.exception})")
            }
        })
    }

    private fun makeToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
