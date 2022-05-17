package net.payrdr.mobile.payment.sdk

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_ERROR
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_PAYMENT
import net.payrdr.mobile.payment.sdk.form.ResultPaymentCallback
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.payment.PaymentActivity
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.core.BuildConfig as BuildConfigCore
import net.payrdr.mobile.payment.sdk.form.BuildConfig as BuildConfigForms
import net.payrdr.mobile.payment.sdk.threeds.BuildConfig as BuildConfigThreeDS

/**
 * The main class for working with the functionality of the payment library from a mobile application.
 */
object SDKPayment {
    private var innerSdkPaymentConfig: SDKPaymentConfig? = null
    internal val sdkPaymentConfig: SDKPaymentConfig
        get() = innerSdkPaymentConfig
            ?: throw IllegalStateException("Please call SDKPayment.init() before.")

    /**
     * Initialization.
     */
    fun init(sdkPaymentConfig: SDKPaymentConfig) {
        innerSdkPaymentConfig = sdkPaymentConfig
    }

    /**
     * Starting the billing cycle process via SDK from [activity].
     *
     * @param activity to which the result will be returned.
     * @param mdOrder order number.
     */
    fun checkout(activity: Activity, mdOrder: String, gPayClicked: Boolean = false) {
        activity.startActivityForResult(
            PaymentActivity.prepareIntent(activity, mdOrder, gPayClicked),
            REQUEST_CODE_PAYMENT
        )
    }

    /**
     * Starting the billing cycle process via SDK from [fragment].
     *
     * @param fragment to which the result will be returned.
     * @param mdOrder order number.
     */
    fun checkout(fragment: Fragment, mdOrder: String, gPayClicked: Boolean = false) {
        fragment.startActivityForResult(
            PaymentActivity.prepareIntent(fragment.requireContext(), mdOrder, gPayClicked),
            REQUEST_CODE_PAYMENT
        )
    }

    fun handleCheckoutResult(
        requestCode: Int,
        data: Intent?,
        paymentCallback: ResultPaymentCallback<PaymentData>
    ): Boolean = if (data != null && REQUEST_CODE_PAYMENT == requestCode) {
        handleCheckoutResult(data, paymentCallback)
        true
    } else {
        false
    }

    private fun handleCheckoutResult(
        data: Intent,
        paymentCallback: ResultPaymentCallback<PaymentData>
    ) {
        val paymentData = data.getParcelableExtra(INTENT_EXTRA_RESULT) as PaymentData?
        if (paymentData != null) {
            paymentCallback.onSuccess(paymentData)
        } else {
            val exception = data.getSerializableExtra(INTENT_EXTRA_ERROR) as SDKException?
            paymentCallback.onFail(exception ?: SDKException("Unknown error"))
        }
    }

    fun getSDKVersion(): String {
        LogDebug.logIfDebug("SDKPayment version is: ${BuildConfig.SDK_PAYMENT_VERSION_NUMBER}")
        LogDebug.logIfDebug("SDKForms version is: ${BuildConfigForms.SDK_FORMS_VERSION_NUMBER}")
        LogDebug.logIfDebug("SDKCore version is: ${BuildConfigCore.SDK_CORE_VERSION_NUMBER}")
        LogDebug.logIfDebug("SDKThreeDS version is: ${BuildConfigThreeDS.SDK_THREEDS_VERSION_NUMBER}")
        return BuildConfig.SDK_PAYMENT_VERSION_NUMBER
    }
}
