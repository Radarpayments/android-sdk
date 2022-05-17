package net.payrdr.mobile.payment.sdk.form

import android.content.Context
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteCardInfoProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.test.core.targetContext
import org.junit.Test

class SDKConfigBuilderTest {

    @Test
    fun succeedBuildWithKeyProviderUrl() {
        SDKConfigBuilder()
            .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
            .build()
    }

    @Test
    fun succeedBuildWithCustomKeyProvider() {
        SDKConfigBuilder()
            .keyProvider(
                CachedKeyProvider(
                    RemoteKeyProvider("https://ecommerce.radarpayments.com/payment/se/keys.do"),
                    targetContext().getSharedPreferences("key", Context.MODE_PRIVATE)
                )
            )
            .build()
    }

    @Test
    fun succeedBuildWithCustomCardInfoProvider() {
        SDKConfigBuilder()
            .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
            .cardInfoProvider(
                RemoteCardInfoProvider(
                    url = "https://mrbin.io/bins/display",
                    urlBin = "https://mrbin.io/bins/"
                )
            )
    }

    @Test(expected = SDKException::class)
    fun buildWithoutKeyProviderShouldThrowException() {
        SDKConfigBuilder()
            .build()
    }

    @Test(expected = SDKException::class)
    fun buildWithTwoDifferentKeyProviderShouldThrowException() {
        SDKConfigBuilder()
            .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
            .keyProvider(RemoteKeyProvider(url = "https://ecommerce.radarpayments.com/payment/se/keys.do"))
            .build()
    }

    @Test(expected = SDKException::class)
    fun buildWithTwoDifferentKeyProviderSwapOrderShouldThrowException() {
        SDKConfigBuilder()
            .keyProvider(RemoteKeyProvider(url = "https://ecommerce.radarpayments.com/payment/se/keys.do"))
            .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
            .build()
    }
}
