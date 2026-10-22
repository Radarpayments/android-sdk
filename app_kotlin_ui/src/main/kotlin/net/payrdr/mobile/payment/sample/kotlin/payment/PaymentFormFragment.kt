package net.payrdr.mobile.payment.sample.kotlin.payment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sample.kotlin.helpers.logRaw
import net.payrdr.mobile.payment.sample.kotlin.helpers.toReadableText
import net.payrdr.mobile.payment.sdk.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult

/**
 * Payment form. The SDK is initialized once in MarketApplication.
 *
 * Started from a fragment, the result comes back to this fragment's onActivityResult. From an activity
 * the calls are the same: SDKPayment.checkout(activity, ...) and SDKPayment.handleCheckoutResult(...)
 * in the activity's onActivityResult.
 */
class PaymentFormFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_payment_form, container, false).apply {
        val mdOrder = findViewById<EditText>(R.id.mdOrder)
        findViewById<Button>(R.id.paymentCheckoutFragment).setOnClickListener {
            SDKPayment.checkout(this@PaymentFormFragment, CheckoutConfig.MdOrder(mdOrder.text.trim().toString()))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // A user cancellation is delivered here too, as PaymentResult.exception of type SDKCanceledByUserException.
        SDKPayment.handleCheckoutResult(requestCode, data, object :
            ResultPaymentCallback<PaymentResult> {
            override fun onResult(result: PaymentResult) {
                result.logRaw()
                view?.findViewById<TextView>(R.id.textResult)?.apply {
                    text = result.toReadableText(requireContext())
                    visibility = View.VISIBLE
                }
            }
        })
    }
}
