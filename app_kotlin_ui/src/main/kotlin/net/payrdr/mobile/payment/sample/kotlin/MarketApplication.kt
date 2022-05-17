@file:Suppress("UndocumentedPublicClass")

package net.payrdr.mobile.payment.sample.kotlin

import android.app.Application
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKForms

@Suppress("UndocumentedPublicClass")
class MarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SDKForms.init(
            SDKConfigBuilder()
                .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
                .build()
        )
    }
}
