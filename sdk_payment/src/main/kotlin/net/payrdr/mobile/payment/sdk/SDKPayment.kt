package net.payrdr.mobile.payment.sdk

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_ERROR
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_PAYMENT
import net.payrdr.mobile.payment.sdk.core.Logger
import net.payrdr.mobile.payment.sdk.exceptions.SDKAlreadyPaymentException
import net.payrdr.mobile.payment.sdk.exceptions.SDKCryptogramException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKOrderNotExistException
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.exceptions.SDKTransactionException
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

    private var use3ds2sdkConfig: Boolean = true
    internal val use3ds2sdk
        get() = use3ds2sdkConfig

    /**
     * Initialization.
     *
     * @param use3ds2sdk using threeDS version 2 or not,
     */
    fun init(sdkPaymentConfig: SDKPaymentConfig, use3ds2sdk: Boolean = true) {
        innerSdkPaymentConfig = sdkPaymentConfig
        use3ds2sdkConfig = use3ds2sdk
    }

    /**
     * Starting the billing cycle process via SDK from [activity].
     *
     * @param activity to which the result will be returned.
     * @param mdOrder order number.
     */
    fun checkout(
        activity: Activity,
        mdOrder: String,
        gPayClicked: Boolean = false
    ) {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "checkout($activity, $mdOrder, $gPayClicked): ",
            null
        )
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
    fun checkout(
        fragment: Fragment,
        mdOrder: String,
        gPayClicked: Boolean = false
    ) {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "checkout($fragment, $mdOrder, $gPayClicked): ",
            null
        )
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

    @Suppress("indent")
    private fun handleCheckoutResult(
        data: Intent,
        paymentCallback: ResultPaymentCallback<PaymentData>
    ) {
        val paymentData = data.getParcelableExtra(INTENT_EXTRA_RESULT) as PaymentData?
        if (paymentData != null) {
            Logger.log(
                this.javaClass,
                Constants.TAG,
                "handleCheckoutResult($data, $paymentCallback): Success " +
                    "PaymentData(${paymentData.mdOrder},${paymentData.status})",
                null
            )
            paymentCallback.onSuccess(paymentData)
        } else {
            val exception = data.getSerializableExtra(INTENT_EXTRA_ERROR) as SDKException?
            Logger.log(
                this.javaClass,
                Constants.TAG,
                "handleCheckoutResult($data, $paymentCallback):" +
                    " Error: ${getExceptionTypesMessage(exception)}",
                exception
            )
            paymentCallback.onFail(exception ?: SDKException("Unknown error"))
        }
    }

    private fun getExceptionTypesMessage(exception: SDKException?): String? {
        return if (exception != null) {
            when (exception) {
                is SDKAlreadyPaymentException -> "payment of a successfully paid order"
                is SDKCryptogramException -> "error while creating cryptogram"
                is SDKDeclinedException -> "order was canceled on previous payment cycle"
                is SDKPaymentApiException -> "error when working with gateway API methods"
                is SDKTransactionException -> "error when creating a transaction when paying through 3ds"
                is SDKOrderNotExistException -> "payment for a non-existent order"
                else -> null
            }
        } else {
            null
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
