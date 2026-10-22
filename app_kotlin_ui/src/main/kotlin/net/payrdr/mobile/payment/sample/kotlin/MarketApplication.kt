@file:Suppress("UndocumentedPublicClass")

package net.payrdr.mobile.payment.sample.kotlin

import android.app.Application
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig

class MarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the SDK once, here: after process death Android recreates SDK screens
        // before any activity of the app, and they need the configuration.
        SDKPayment.init(
            SDKPaymentConfig(
                baseURL = BASE_URL,
                // For a gateway with a private CA, pass its certificate:
                // sslContextConfig = SSLContextCustomCAFactory.fromBase64String("<base64 certificate>"),
            )
        )
    }

    companion object {
        const val BASE_URL = "https://dev.bpcbt.com/payment"
    }
}
