@file:Suppress("UndocumentedPublicClass")

package net.payrdr.mobile.payment.sample.kotlin

import android.app.Application
import java.io.InputStream
import net.payrdr.mobile.payment.sdk.form.component.impl.SSLContextConfig
import net.payrdr.mobile.payment.sdk.form.component.impl.SSLContextCustomCAFactory

@Suppress("UndocumentedPublicClass")
class MarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //    1) Example without ssl context configuration for sdk_form module:
        //
        //    SDKForms.init(
        //        SDKConfigBuilder()
        //            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
        //            .build()
        //    )

        //    2) Example of configuration with custom sslContext for sdk_form module :
        //
        //    val sslContextConfig = SSLContextCustomCAFactory.fromPem()
        //    val sslContextConfig = SSLContextCustomCAFactory.fromBase64String()
        //    val sslContextConfig = SSLContextCustomCAFactory.fromInputStream()
        //
        //    val keyProvider = RemoteKeyProvider("https://dev.bpcbt.com/payment/se/keys.do")
        //
        //    SDKForms.init(
        //        SDKConfigBuilder()
        //            .keyProvider(keyProvider)
        //            .setSslContextConfig(sslContextConfig.sslContext)
        //            .build()
        //    )

        //   3) Example
        //   val cert: InputStream = baseContext.resources.openRawResource(R.raw.custom_cert)
        //   sslContextConfig = SSLContextCustomCAFactory.fromInputStream(cert)
    }

    companion object {
        // If you want launch example for 3ds payment with ssl context you need define
        // this variable with your ssl certificate onCreate method (look at "3) Example")
        var sslContextConfig: SSLContextConfig? = null
    }
}
