package net.payrdr.mobile.payment.sdk.form

import android.content.Context
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteCardInfoProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.test.core.targetContext
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
class SDKConfigBuilderTest {

    @Test
    @Description("succeedBuildWithKeyProviderUrl")
    fun succeedBuildWithKeyProviderUrl() {
        SDKConfigBuilder()
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
            .build()
    }

    @Test
    @Description("succeedBuildWithCustomKeyProvider")
    fun succeedBuildWithCustomKeyProvider() {
        SDKConfigBuilder()
            .keyProvider(
                CachedKeyProvider(
                    RemoteKeyProvider("https://dev.bpcbt.com/payment/se/keys.do"),
                    targetContext().getSharedPreferences("key", Context.MODE_PRIVATE)
                )
            )
            .build()
    }

    @Test
    @Description("succeedBuildWithCustomCardInfoProvider")
    fun succeedBuildWithCustomCardInfoProvider() {
        SDKConfigBuilder()
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
            .cardInfoProvider(
                RemoteCardInfoProvider(
                    url = "https://mrbin.io/bins/display",
                    urlBin = "https://mrbin.io/bins/"
                )
            )
    }

    @Test(expected = SDKException::class)
    @Description("buildWithoutKeyProviderShouldThrowException")
    fun buildWithoutKeyProviderShouldThrowException() {
        SDKConfigBuilder()
            .build()
    }

    @Test(expected = SDKException::class)
    @Description("buildWithTwoDifferentKeyProviderShouldThrowException")
    fun buildWithTwoDifferentKeyProviderShouldThrowException() {
        SDKConfigBuilder()
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
            .keyProvider(RemoteKeyProvider(url = "https://dev.bpcbt.com/payment/se/keys.do"))
            .build()
    }

    @Test(expected = SDKException::class)
    @Description("buildWithTwoDifferentKeyProviderSwapOrderShouldThrowException")
    fun buildWithTwoDifferentKeyProviderSwapOrderShouldThrowException() {
        SDKConfigBuilder()
            .keyProvider(RemoteKeyProvider(url = "https://dev.bpcbt.com/payment/se/keys.do"))
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do")
            .build()
    }
}
