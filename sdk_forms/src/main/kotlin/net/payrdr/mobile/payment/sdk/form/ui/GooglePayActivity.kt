package net.payrdr.mobile.payment.sdk.form.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayUtils
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentDataStatus
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoGooglePay
import net.payrdr.mobile.payment.sdk.form.utils.finishWithResult
import org.json.JSONObject
import com.google.android.gms.wallet.PaymentData as GPaymentData

/**
 * Screen to start the payment process via Google Pay.
 */
class GooglePayActivity : BaseActivity() {

    private var cryptogramProcessor: CryptogramProcessor = SDKForms.cryptogramProcessor
    private val config: GooglePayPaymentConfig by lazy {
        intent.getParcelableExtra<GooglePayPaymentConfig>(Constants.INTENT_EXTRA_CONFIG)!! as GooglePayPaymentConfig
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val environment = if (config.testEnvironment) {
            WalletConstants.ENVIRONMENT_TEST
        } else {
            WalletConstants.ENVIRONMENT_PRODUCTION
        }
        val paymentsClient = GooglePayUtils.createPaymentsClient(this, environment)
        requestPayment(
            paymentsClient = paymentsClient,
            activity = this,
            config = config
        )
    }

    private fun requestPayment(
        paymentsClient: PaymentsClient,
        activity: Activity,
        config: GooglePayPaymentConfig
    ) {
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(config.paymentData),
            activity, Constants.REQUEST_CODE_GPAY_LOAD_PAYMENT_DATA
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_CODE_GPAY_LOAD_PAYMENT_DATA -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val paymentData: GPaymentData? = GPaymentData.getFromIntent(data!!)
                        paymentData?.let {
                            val json = it.toJson()
                            val paymentMethodData = JSONObject(json)
                                .getJSONObject("paymentMethodData")
                            val paymentToken = paymentMethodData
                                .getJSONObject("tokenizationData")
                                .getString("token")

                            handlePaymentData(paymentToken)
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        finish()
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        finish()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handlePaymentData(token: String) {
        workScope.launch(Dispatchers.Main) {
            val seToken = cryptogramProcessor.create(token)
            finishWithResult(
                CryptogramData(
                    status = PaymentDataStatus.SUCCEEDED,
                    seToken = seToken,
                    info = PaymentInfoGooglePay(
                        order = config.order
                    )
                )
            )
        }
    }

    companion object {

        /**
         * Prepare the [Intent] to launch the payment screen via Google Pay.
         *
         * @param context for the preparation of intent.
         * @param config payment configuration.
         */
        fun prepareIntent(
            context: Context,
            config: GooglePayPaymentConfig
        ): Intent = Intent(context, GooglePayActivity::class.java).apply {
            putExtra(Constants.INTENT_EXTRA_CONFIG, config)
        }
    }
}
