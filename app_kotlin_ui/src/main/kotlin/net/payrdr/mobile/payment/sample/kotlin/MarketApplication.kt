@file:Suppress("UndocumentedPublicClass")

package net.payrdr.mobile.payment.sample.kotlin

import android.app.Application
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.SSLContextCustomCAFactory

@Suppress("UndocumentedPublicClass")
class MarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SDKForms.init(
            SDKConfigBuilder()
                .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
                .build()
        )

        //    Example of configuration with custom sslContext:
        //
        //    val sslContext = SSLContextCustomCAFactory.fromPem()
        //    val sslContext = SSLContextCustomCAFactory.fromBase64String()
        //    val sslContext = SSLContextCustomCAFactory.fromInputStream()
        //
        //    val keyProvider = RemoteKeyProvider("https://ecommerce.radarpayments.com/payment/se/keys.do", sslContext)
        //
        //    SDKForms.init(
        //        SDKConfigBuilder()
        //            .keyProvider(keyProvider)
        //            .build()
        //    )
    }
}
