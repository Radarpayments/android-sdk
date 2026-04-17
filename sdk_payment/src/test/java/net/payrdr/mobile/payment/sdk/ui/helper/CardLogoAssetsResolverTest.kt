package net.payrdr.mobile.payment.sdk.ui.helper

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

@SmallTest
class CardLogoAssetsResolverTest {

    private fun contextWithAssets(assets: Array<String>): Context {
        val context = mockk<Context>()
        val resources = mockk<Resources>()
        val assetManager = mockk<AssetManager>()
        every { context.resources } returns resources
        every { resources.assets } returns assetManager
        every { assetManager.list("payment_system") } returns assets
        return context
    }

    // ---- resolveByPan ----

    @Test
    fun shouldResolveVisaByPanPrefix() {
        val context = contextWithAssets(arrayOf("logo-visa.svg"))

        CardLogoAssetsResolver.resolveByPan(context, "4111111111111111") shouldBe
            "payment_system/logo-visa.svg"
    }

    @Test
    fun shouldResolveMastercardByClassicPrefix() {
        val context = contextWithAssets(arrayOf("logo-mastercard.svg"))

        CardLogoAssetsResolver.resolveByPan(context, "5555555555554444") shouldBe
            "payment_system/logo-mastercard.svg"
    }

    @Test
    fun shouldResolveAmexByPrefix() {
        val context = contextWithAssets(arrayOf("logo-amex.svg"))

        CardLogoAssetsResolver.resolveByPan(context, "378282246310005") shouldBe
            "payment_system/logo-amex.svg"
    }

    @Test
    fun shouldPreferLightVariantWhenRequestedAndAvailable() {
        val context = contextWithAssets(arrayOf("logo-visa.svg", "logo-visa-white.svg"))

        CardLogoAssetsResolver.resolveByPan(context, "4111111111111111", preferLight = true) shouldBe
            "payment_system/logo-visa-white.svg"
    }

    @Test
    fun shouldFallbackToDefaultWhenLightVariantMissing() {
        val context = contextWithAssets(arrayOf("logo-visa.svg"))

        CardLogoAssetsResolver.resolveByPan(context, "4111111111111111", preferLight = true) shouldBe
            "payment_system/logo-visa.svg"
    }

    @Test
    fun shouldStripNonDigitsBeforeMatching() {
        val context = contextWithAssets(arrayOf("logo-visa.svg"))

        CardLogoAssetsResolver.resolveByPan(context, "4111-1111-1111-1111") shouldBe
            "payment_system/logo-visa.svg"
    }

    @Test
    fun shouldReturnNullWhenPanDoesNotMatchAnyPaymentSystem() {
        val context = contextWithAssets(arrayOf("logo-visa.svg", "logo-mastercard.svg"))

        CardLogoAssetsResolver.resolveByPan(context, "9999999999999999") shouldBe null
    }

    @Test
    fun shouldReturnNullWhenAssetIsMissingForMatchedSystem() {
        val context = contextWithAssets(emptyArray())

        CardLogoAssetsResolver.resolveByPan(context, "4111111111111111") shouldBe null
    }

    // ---- resolveByName ----

    @Test
    fun shouldResolveByExplicitName() {
        val context = contextWithAssets(arrayOf("logo-jcb.svg"))

        CardLogoAssetsResolver.resolveByName(context, "jcb") shouldBe
            "payment_system/logo-jcb.svg"
    }

    @Test
    fun shouldReturnNullWhenAssetsListIsNull() {
        val context = mockk<Context>()
        val resources = mockk<Resources>()
        val assetManager = mockk<AssetManager>()
        every { context.resources } returns resources
        every { resources.assets } returns assetManager
        every { assetManager.list("payment_system") } returns null

        CardLogoAssetsResolver.resolveByName(context, "visa") shouldBe null
    }

    @Test
    fun shouldReturnNullForUnknownSystemName() {
        val context = contextWithAssets(arrayOf("logo-visa.svg"))

        CardLogoAssetsResolver.resolveByName(context, "mir") shouldBe null
    }
}
