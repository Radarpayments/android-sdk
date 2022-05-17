package net.payrdr.mobile.payment.sample.kotlin.payment

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
import net.payrdr.mobile.payment.sdk.exceptions.SDKAlreadyPaymentException
import net.payrdr.mobile.payment.sdk.exceptions.SDKCryptogramException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKOrderNotExistException
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.exceptions.SDKTransactionException
import net.payrdr.mobile.payment.sdk.form.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
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
            /* spellchecker: disable */
            val dsRoot =
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
            val paymentConfig = SDKPaymentConfig("https://ecommerce.radarpayments.com/payment/rest", dsRoot)
            SDKPayment.init(paymentConfig)
            SDKPayment.checkout(this, mdOrder.text.trim().toString())
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Processing the result of the payment cycle.
        SDKPayment.handleCheckoutResult(requestCode, data, object :
            ResultPaymentCallback<PaymentData> {
            override fun onSuccess(result: PaymentData) {
                // Order payment result.
                val resData = "PaymentData(${result.mdOrder}, ${result.status})"
                activity?.log(resData)
                view?.findViewById<TextView>(R.id.textResult)?.text = resData
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
                    else -> activity?.log("${e.message} ${e.cause}")
                }
            }
        })
    }

    private fun makeToast(text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
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
    }
}
