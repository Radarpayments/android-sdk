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
import net.payrdr.mobile.payment.sdk.payment.model.Use3DSConfig

class PaymentFormActivity : AppCompatActivity() {

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

        val paymentConfig = SDKPaymentConfig(
            baseUrl,
            use3DSConfig = Use3DSConfig.Use3ds2sdk(dsRoot),
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
