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
        SDKFormsConfigBuilder()
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do", null)
            .build()
    }

    @Test
    @Description("succeedBuildWithCustomKeyProvider")
    fun succeedBuildWithCustomKeyProvider() {
        SDKFormsConfigBuilder()
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
        SDKFormsConfigBuilder()
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do", null)
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
        SDKFormsConfigBuilder()
            .build()
    }

    @Test(expected = SDKException::class)
    @Description("buildWithTwoDifferentKeyProviderShouldThrowException")
    fun buildWithTwoDifferentKeyProviderShouldThrowException() {
        SDKFormsConfigBuilder()
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do", null)
            .keyProvider(RemoteKeyProvider(url = "https://dev.bpcbt.com/payment/se/keys.do"))
            .build()
    }

    @Test(expected = SDKException::class)
    @Description("buildWithTwoDifferentKeyProviderSwapOrderShouldThrowException")
    fun buildWithTwoDifferentKeyProviderSwapOrderShouldThrowException() {
        SDKFormsConfigBuilder()
            .keyProvider(RemoteKeyProvider(url = "https://dev.bpcbt.com/payment/se/keys.do"))
            .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do", null)
            .build()
    }
}
