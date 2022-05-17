package net.payrdr.mobile.payment.sdk.form

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import net.payrdr.mobile.payment.sdk.core.component.impl.DefaultPaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.component.impl.RSACryptogramCipher
import net.payrdr.mobile.payment.sdk.form.Constants.REQUEST_CODE_CRYPTOGRAM
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.component.impl.DefaultCryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.SDKConfig
import net.payrdr.mobile.payment.sdk.form.ui.CardListActivity
import net.payrdr.mobile.payment.sdk.form.ui.CardNewActivity
import net.payrdr.mobile.payment.sdk.form.ui.GooglePayActivity
import net.payrdr.mobile.payment.sdk.form.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.form.ui.helper.ThemeSetting
import net.payrdr.mobile.payment.sdk.core.BuildConfig as BuildConfigCore

/**
 * The main class for working with the functionality of the user interface library from a mobile application.
 */
object SDKForms {

    @JvmSynthetic
    internal var innerSdkConfig: SDKConfig? = null
    internal val sdkConfig: SDKConfig
        get() = innerSdkConfig
            ?: throw IllegalStateException("Please call SDKForms.init() before.")

    @JvmSynthetic
    internal var innerCryptogramProcessor: CryptogramProcessor? = null
        get() {
            return field ?: DefaultCryptogramProcessor(
                keyProvider = sdkConfig.keyProvider,
                paymentStringProcessor = DefaultPaymentStringProcessor(),
                cryptogramCipher = RSACryptogramCipher()
            )
        }
    internal val cryptogramProcessor: CryptogramProcessor
        get() {
            return innerCryptogramProcessor
                ?: throw IllegalStateException("Please call SDKForms.init() before.")
        }

    /**
     * Initialization.
     */
    fun init(sdkConfig: SDKConfig) {
        innerSdkConfig = sdkConfig
    }

    /**
     * return SDKForms version
     */
    fun getSDKVersion(): String {
        Log.d("SDKForms", "SDKForms version is: ${BuildConfig.SDK_FORMS_VERSION_NUMBER}")
        Log.d("SDKForms", "SDKCore version is: ${BuildConfigCore.SDK_CORE_VERSION_NUMBER}")
        return BuildConfig.SDK_FORMS_VERSION_NUMBER
    }

    /**
     * Launching the payment process from [Activity].
     *
     * @param activity in which the result will be returned.
     * @param config payment configuration.
     */
    fun cryptogram(activity: Activity, config: PaymentConfig) {
        checkNotNull(cryptogramProcessor)
        LocalizationSetting.setLanguage(config.locale)
        ThemeSetting.setTheme(config.theme)
        if (config.cards.isEmpty()) {
            activity.startActivityForResult(
                CardNewActivity.prepareIntent(activity, config),
                REQUEST_CODE_CRYPTOGRAM
            )
        } else {
            activity.startActivityForResult(
                CardListActivity.prepareIntent(activity, config),
                REQUEST_CODE_CRYPTOGRAM
            )
        }
    }

    /**
     * Launching the payment process for Google Pay payment from [Activity].
     *
     * @param activity in which the result will be returned.
     * @param config payment configuration.
     */
    fun cryptogram(activity: Activity, config: GooglePayPaymentConfig) {
        checkNotNull(cryptogramProcessor)
        LocalizationSetting.setLanguage(config.locale)
        ThemeSetting.setTheme(config.theme)
        activity.startActivityForResult(
            GooglePayActivity.prepareIntent(activity, config),
            REQUEST_CODE_CRYPTOGRAM
        )
    }

    /**
     * Launching the payment process from [Fragment].
     *
     * @param fragment in which the result will be returned.
     * @param config payment configuration.
     */
    fun cryptogram(fragment: Fragment, config: PaymentConfig) {
        checkNotNull(cryptogramProcessor)
        LocalizationSetting.setLanguage(config.locale)
        ThemeSetting.setTheme(config.theme)
        if (config.cards.isEmpty()) {
            fragment.startActivityForResult(
                CardNewActivity.prepareIntent(fragment.requireContext(), config),
                REQUEST_CODE_CRYPTOGRAM
            )
        } else {
            fragment.startActivityForResult(
                CardListActivity.prepareIntent(fragment.requireContext(), config),
                REQUEST_CODE_CRYPTOGRAM
            )
        }
    }

    /**
     * Launching the payment process for Google Pay payment from [Fragment].
     *
     * @param fragment in which the result will be returned.
     * @param config payment configuration.
     */
    fun cryptogram(fragment: Fragment, config: GooglePayPaymentConfig) {
        checkNotNull(cryptogramProcessor)
        LocalizationSetting.setLanguage(config.locale)
        ThemeSetting.setTheme(config.theme)
        fragment.startActivityForResult(
            GooglePayActivity.prepareIntent(fragment.requireContext(), config),
            REQUEST_CODE_CRYPTOGRAM
        )
    }

    /**
     * Processing the result of obtaining a cryptogram.
     */
    fun handleCryptogramResult(
        requestCode: Int,
        data: Intent?,
        cryptogramCallback: ResultCryptogramCallback<CryptogramData>
    ): Boolean = if (data != null && REQUEST_CODE_CRYPTOGRAM == requestCode) {
        handleCryptogramResult(data, cryptogramCallback)
        true
    } else {
        false
    }

    private fun handleCryptogramResult(
        data: Intent,
        cryptogramCallback: ResultCryptogramCallback<CryptogramData>
    ) {
        val cryptogramData =
            data.getParcelableExtra(Constants.INTENT_EXTRA_RESULT) as CryptogramData?
        if (cryptogramData != null) {
            cryptogramCallback.onSuccess(cryptogramData)
        } else {
            val exception = data.getSerializableExtra(Constants.INTENT_EXTRA_ERROR) as SDKException?
            cryptogramCallback.onFail(exception ?: SDKException("Unknown error"))
        }
    }
}
