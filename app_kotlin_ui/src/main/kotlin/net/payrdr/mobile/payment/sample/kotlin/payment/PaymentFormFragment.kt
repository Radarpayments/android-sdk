package net.payrdr.mobile.payment.sample.kotlin.payment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_payment_form.mdOrder
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sample.kotlin.helpers.log
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig

class PaymentFormFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_form, container, false)
        view.setBackgroundColor(Color.WHITE)
        view.findViewById<Button>(R.id.paymentCheckoutFragment).setOnClickListener {
            val paymentConfig = SDKPaymentConfig(
                "https://dev.bpcbt.com/payment",
            )
            SDKPayment.init(paymentConfig)
            SDKPayment.checkout(this, CheckoutConfig.MdOrder(mdOrder.text.trim().toString()))
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED) {
            makeToast("Canceled payment by user")
        }

        // Processing the result of the payment cycle.
        SDKPayment.handleCheckoutResult(requestCode, data, object :
            ResultPaymentCallback<PaymentResult> {
            override fun onResult(result: PaymentResult) {
                // Order payment result.
                val resData =
                    "PaymentData(${result.sessionId}, ${result.isSuccess} ${result.exception})"
                activity?.log(resData)
                view?.findViewById<TextView>(R.id.textResult)?.text = resData
            }
        })
    }

    private fun makeToast(text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }
}
