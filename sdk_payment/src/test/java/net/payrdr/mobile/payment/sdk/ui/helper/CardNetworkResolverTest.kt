package net.payrdr.mobile.payment.sdk.ui.helper

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.junit.Test

@SmallTest
class CardNetworkResolverTest {

    @Test
    fun shouldResolveVisa() {
        CardNetworkResolver.resolve("4111111111111111") shouldBe CardNetwork.VISA
        CardNetworkResolver.resolve("4") shouldBe CardNetwork.VISA
        CardNetworkResolver.resolve("4539 1488 0343 6467") shouldBe CardNetwork.VISA
    }

    @Test
    fun shouldResolveMastercard() {
        CardNetworkResolver.resolve("5100000000000000") shouldBe CardNetwork.MASTERCARD
        CardNetworkResolver.resolve("5599999999999999") shouldBe CardNetwork.MASTERCARD
        CardNetworkResolver.resolve("2221000000000000") shouldBe CardNetwork.MASTERCARD
        CardNetworkResolver.resolve("2720999999999999") shouldBe CardNetwork.MASTERCARD
        CardNetworkResolver.resolve("2220000000000000") shouldBe CardNetwork.UNKNOWN
        CardNetworkResolver.resolve("2721000000000000") shouldBe CardNetwork.UNKNOWN
    }

    @Test
    fun shouldResolveAmex() {
        CardNetworkResolver.resolve("340000000000000") shouldBe CardNetwork.AMEX
        CardNetworkResolver.resolve("370000000000000") shouldBe CardNetwork.AMEX
    }

    @Test
    fun shouldNotRecogniseMir() {
        CardNetworkResolver.resolve("2200000000000000") shouldBe CardNetwork.UNKNOWN
        CardNetworkResolver.resolve("2204999999999999") shouldBe CardNetwork.UNKNOWN
        CardNetworkResolver.resolve("220000**0000") shouldBe CardNetwork.UNKNOWN
    }

    @Test
    fun shouldResolveUnionPay() {
        CardNetworkResolver.resolve("6200000000000000") shouldBe CardNetwork.UNIONPAY
        CardNetworkResolver.resolve("6221260000000000") shouldBe CardNetwork.UNIONPAY
    }

    @Test
    fun shouldResolveJcb() {
        CardNetworkResolver.resolve("3528000000000000") shouldBe CardNetwork.JCB
        CardNetworkResolver.resolve("3589999999999999") shouldBe CardNetwork.JCB
        CardNetworkResolver.resolve("3527000000000000") shouldBe CardNetwork.UNKNOWN
    }

    @Test
    fun shouldResolveDiners() {
        CardNetworkResolver.resolve("36000000000000") shouldBe CardNetwork.DINERS
        CardNetworkResolver.resolve("30500000000000") shouldBe CardNetwork.DINERS
        CardNetworkResolver.resolve("3800000000000000") shouldBe CardNetwork.DINERS
        CardNetworkResolver.resolve("3060000000000000") shouldBe CardNetwork.UNKNOWN
    }

    @Test
    fun shouldResolveDiscover() {
        CardNetworkResolver.resolve("6011000000000000") shouldBe CardNetwork.DISCOVER
        CardNetworkResolver.resolve("6440000000000000") shouldBe CardNetwork.DISCOVER
        CardNetworkResolver.resolve("6500000000000000") shouldBe CardNetwork.DISCOVER
    }

    @Test
    fun shouldResolveMaestro() {
        CardNetworkResolver.resolve("5018000000000000") shouldBe CardNetwork.MAESTRO
        CardNetworkResolver.resolve("5600000000000000") shouldBe CardNetwork.MAESTRO
        CardNetworkResolver.resolve("6304000000000000") shouldBe CardNetwork.MAESTRO
        CardNetworkResolver.resolve("6761000000000000") shouldBe CardNetwork.MAESTRO
    }

    @Test
    fun shouldResolveUnknown() {
        CardNetworkResolver.resolve("") shouldBe CardNetwork.UNKNOWN
        CardNetworkResolver.resolve("1") shouldBe CardNetwork.UNKNOWN
        CardNetworkResolver.resolve("9999999999999999") shouldBe CardNetwork.UNKNOWN
    }

    @Test
    fun shouldIgnoreSeparatorsAndMask() {
        CardNetworkResolver.resolve("4111-1111-1111-1111") shouldBe CardNetwork.VISA
        CardNetworkResolver.resolve("  5111 2222 3333 4444 ") shouldBe CardNetwork.MASTERCARD
        CardNetworkResolver.resolve("555555**5599") shouldBe CardNetwork.MASTERCARD
    }
}
