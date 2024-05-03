package net.payrdr.mobile.payment.sdk.core

import android.content.Intent
import net.payrdr.mobile.payment.sdk.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult

class ResultHandlerHelper {

    var paymentData: PaymentResult? = null
        private set

    val onActivityResult: ((requestCode: Int, resultCode: Int, data: Intent?) -> Unit) =
        { requestCode, _, data ->
            SDKPayment.handleCheckoutResult(
                requestCode,
                data,
                object : ResultPaymentCallback<PaymentResult> {
                    override fun onResult(result: PaymentResult) {
                        paymentData = result
                    }
                }
            )
        }

    fun resetPaymentData() {
        paymentData = null
    }
}
