@file:Suppress("UndocumentedPublicClass")

package net.payrdr.mobile.payment.sample.kotlin

import android.app.Application

@Suppress("UndocumentedPublicClass")
class MarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //    SDKForms.init(
        //        SDKConfigBuilder()
        //            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
        //            .build()
        //    )

        //    Example of configuration with custom sslContext:
        //
        //    val sslContextConfig = SSLContextCustomCAFactory.fromPem()
        //    val sslContextConfig = SSLContextCustomCAFactory.fromBase64String()
        //    val sslContextConfig = SSLContextCustomCAFactory.fromInputStream()
        //
        //    val keyProvider = RemoteKeyProvider("https://dev.bpcbt.com/payment/se/keys.do", sslContextConfig.sslContext)
        //
        //    SDKForms.init(
        //        SDKConfigBuilder()
        //            .keyProvider(keyProvider)
        //            .build()
        //    )
    }
}
