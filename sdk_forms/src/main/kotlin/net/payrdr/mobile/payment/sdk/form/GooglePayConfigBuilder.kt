package net.payrdr.mobile.payment.sdk.form

import com.google.android.gms.wallet.PaymentDataRequest
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.Theme
import net.payrdr.mobile.payment.sdk.logs.Logger
import java.util.Locale
import java.util.UUID

/**
 * Constructor for forming a payment configuration via the Google Pay button.
 *
 * @param order identifier of the paid order.
 * @param paymentData information for making a payment.
 * @param gateway name of merchant
 * @param gatewayMerchantId merchatntId for gateway
 */
@Suppress("TooManyFunctions")
class GooglePayConfigBuilder(
    private val order: String = "",
    private val paymentData: PaymentDataRequest,
    private var gateway: String,
    private var gatewayMerchantId: String,
) {
    private var theme: Theme = Theme.SYSTEM
    private var locale: Locale = Locale.getDefault()
    private var uuid: String = UUID.randomUUID().toString()
    private var timestamp: Long = System.currentTimeMillis()
    private var testEnvironment: Boolean = false

    /**
     * Option to control the theme of the interface.
     *
     * Optional, default SYSTEM.
     *
     * @param theme setting of the card scanning function.
     * @return the current constructor.
     */
    fun theme(theme: Theme): GooglePayConfigBuilder = apply {
        Logger.info(
            this.javaClass,
            Constants.TAG,
            "theme($theme): Option to control the theme",
            null
        )
        this.theme = theme
    }

    /**
     * Installation of localization.
     *
     * Optionally, the localization of the shape of the floor is determined automatically.
     *
     * @param locale localization.
     * @return the current constructor.
     */
    fun locale(locale: Locale): GooglePayConfigBuilder = apply {
        Logger.info(
            this.javaClass,
            Constants.TAG,
            "locale($locale): Installation of localization.",
            null
        )
        this.locale = locale
    }

    /**
     * Setting a unique identifier for the payment.
     *
     * Optionally, a unique payment identifier is generated automatically.
     *
     * @param uuid payment identifier.
     * @return the current constructor.
     */
    fun uuid(uuid: String): GooglePayConfigBuilder = apply {
        Logger.info(
            this.javaClass,
            Constants.TAG,
            "uuid($uuid): Setting a unique identifier for the payment.",
            null
        )
        this.uuid = uuid
    }

    /**
     * Setting the time of formation of payment.
     *
     * Optionally, the time of formation of the payment is set automatically.
     *
     * @param timestamp time of payment formation.
     * @return the current constructor.
     */
    fun timestamp(timestamp: Long): GooglePayConfigBuilder = apply {
        Logger.info(
            this.javaClass,
            Constants.TAG,
            "timestamp($timestamp): Setting the time of formation of payment.",
            null
        )
        this.timestamp = timestamp
    }

    /**
     * Setting the flag to use the test environment.
     *
     * Optional, default false.
     *
     * @param testEnvironment flag for using the test environment.
     * @return the current constructor.
     */
    fun testEnvironment(testEnvironment: Boolean): GooglePayConfigBuilder = apply {
        Logger.info(
            this.javaClass,
            Constants.TAG,
            "testEnvironment($testEnvironment): Setting the flag to use the test environment.",
            null
        )
        this.testEnvironment = testEnvironment
    }

    /**
     * Creates a payment configuration via the Google Pay button.
     *
     * @return payment configuration.
     */
    fun build() = GooglePayPaymentConfig(
        order = this.order,
        paymentData = this.paymentData,
        uuid = this.uuid,
        theme = this.theme,
        locale = this.locale,
        timestamp = this.timestamp,
        testEnvironment = this.testEnvironment,
        gateway = this.gateway,
        gatewayMerchantId = this.gatewayMerchantId,
    )
}
