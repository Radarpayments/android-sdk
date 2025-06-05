package net.payrdr.mobile.payment.sdk

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_PAYMENT
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.SDKFormsConfigBuilder
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.logs.Logger
import net.payrdr.mobile.payment.sdk.payment.PaymentActivity
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.payment.model.PaymentApiVersion
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.utils.SessionIdConverter
import net.payrdr.mobile.payment.sdk.core.BuildConfig as BuildConfigCore
import net.payrdr.mobile.payment.sdk.form.BuildConfig as BuildConfigForms

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
     *
     * @param sdkPaymentConfig - sdk configuration.
     */
    fun init(sdkPaymentConfig: SDKPaymentConfig) {
        innerSdkPaymentConfig = sdkPaymentConfig

        val sdkFormsConfig = SDKFormsConfigBuilder()
            .keyProvider(
                RemoteKeyProvider(
                    "${sdkPaymentConfig.baseURL}/se/keys.do",
                    sdkPaymentConfig.sslContextConfig?.sslContext,
                )
            ).build()

        SDKForms.init(sdkFormsConfig)
    }

    /**
     * Starting the billing cycle process via SDK from [activity].
     *
     * @param activity to which the result will be returned.
     * @param checkoutConfig configuration for checkout
     */
    fun checkout(
        activity: Activity,
        checkoutConfig: CheckoutConfig,
        gPayClicked: Boolean = false
    ) {
        when (checkoutConfig) {
            is CheckoutConfig.MdOrder -> {
                val mdOrder = checkoutConfig.value
                Logger.info(
                    this.javaClass,
                    Constants.TAG,
                    "checkout($activity, $mdOrder, $gPayClicked): ",
                    null
                )
                activity.startActivityForResult(
                    PaymentActivity.prepareIntent(
                        activity,
                        mdOrder,
                        gPayClicked,
                        PaymentApiVersion.V1
                    ),
                    REQUEST_CODE_PAYMENT
                )
            }

            is CheckoutConfig.SessionId -> {
                val sessionId = checkoutConfig.value
                val mdOrder = SessionIdConverter.sessionIdToMdOrder(sessionId)
                Logger.info(
                    this.javaClass,
                    Constants.TAG,
                    "checkout($activity, $mdOrder, $sessionId, $gPayClicked): ",
                    null
                )
                activity.startActivityForResult(
                    PaymentActivity.prepareIntent(
                        activity,
                        mdOrder,
                        gPayClicked,
                        PaymentApiVersion.V2
                    ),
                    REQUEST_CODE_PAYMENT
                )
            }
        }
    }

    /**
     * Starting the billing cycle process via SDK from [fragment].
     *
     * @param fragment to which the result will be returned.
     * @param checkoutConfig configuration for checkout
     */
    fun checkout(
        fragment: Fragment,
        checkoutConfig: CheckoutConfig,
        gPayClicked: Boolean = false
    ) {
        when (checkoutConfig) {
            is CheckoutConfig.MdOrder -> {
                val mdOrder = checkoutConfig.value
                Logger.info(
                    this.javaClass,
                    Constants.TAG,
                    "checkout($fragment, $mdOrder, $gPayClicked): ",
                    null
                )
                fragment.startActivityForResult(
                    PaymentActivity.prepareIntent(
                        fragment.requireContext(),
                        mdOrder,
                        gPayClicked,
                        PaymentApiVersion.V1
                    ),
                    REQUEST_CODE_PAYMENT
                )
            }

            is CheckoutConfig.SessionId -> {
                val sessionId = checkoutConfig.value
                val mdOrder = SessionIdConverter.sessionIdToMdOrder(sessionId)
                Logger.info(
                    this.javaClass,
                    Constants.TAG,
                    "checkout($fragment, $sessionId, $gPayClicked): ",
                    null
                )
                fragment.startActivityForResult(
                    PaymentActivity.prepareIntent(
                        fragment.requireContext(),
                        mdOrder,
                        gPayClicked,
                        PaymentApiVersion.V2
                    ),
                    REQUEST_CODE_PAYMENT
                )
            }
        }
    }

    fun handleCheckoutResult(
        requestCode: Int,
        data: Intent?,
        paymentCallback: ResultPaymentCallback<PaymentResult>
    ): Boolean = if (data != null && REQUEST_CODE_PAYMENT == requestCode) {
        handleCheckoutResult(data, paymentCallback)
        true
    } else {
        false
    }

    @Suppress("indent")
    private fun handleCheckoutResult(
        data: Intent,
        paymentCallback: ResultPaymentCallback<PaymentResult>
    ) {
        val paymentData = data.getParcelableExtra(INTENT_EXTRA_RESULT) as PaymentResult?
        if (paymentData != null) {
            Logger.info(
                this.javaClass,
                Constants.TAG,
                "handleCheckoutResult($data, $paymentCallback): Success " +
                        "PaymentData(${paymentData.sessionId},${paymentData.isSuccess})",
                null
            )
            paymentCallback.onResult(paymentData)
            if (paymentData.isSuccess.not()) {
                Logger.uploadLogs()
            }
        } else {
            Logger.error(
                this.javaClass,
                Constants.TAG,
                "handleCheckoutResult $data",
                SDKException("Error handle result")
            )
            paymentCallback.onResult(
                PaymentResult(
                    sessionId = "",
                    isSuccess = false,
                    exception = SDKException("Error handle result"),
                )
            )
            Logger.uploadLogs()
        }
    }

    fun getSDKVersion(): String {
        LogDebug.logIfDebug("SDKPayment version is: ${BuildConfig.SDK_PAYMENT_VERSION_NUMBER}")
        LogDebug.logIfDebug("SDKForms version is: ${BuildConfigForms.SDK_FORMS_VERSION_NUMBER}")
        LogDebug.logIfDebug("SDKCore version is: ${BuildConfigCore.SDK_CORE_VERSION_NUMBER}")
        return BuildConfig.SDK_PAYMENT_VERSION_NUMBER
    }
}
