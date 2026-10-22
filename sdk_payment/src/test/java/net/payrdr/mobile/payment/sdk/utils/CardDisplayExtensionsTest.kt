package net.payrdr.mobile.payment.sdk.utils

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import org.junit.Test

@SmallTest
class CardDisplayExtensionsTest {

    @Test
    @Description("stored card number from the gateway shows the first and last 4 digits")
    fun `masks stored card number`() {
        "411111**1111".maskedPanForDisplay() shouldBe "4111 •••• •••• 1111"
    }

    @Test
    @Description("full card number is masked the same way")
    fun `masks full card number`() {
        "5555555555554444".maskedPanForDisplay() shouldBe "5555 •••• •••• 4444"
    }

    @Test
    @Description("too short value is returned as is")
    fun `keeps short value`() {
        "1111".maskedPanForDisplay() shouldBe "1111"
    }

    @Test
    @Description("expiry is shown as MM/YY for a four-digit year")
    fun `formats expiry with four digit year`() {
        ExpiryDate(expYear = 2026, expMonth = 12).toDisplayString() shouldBe "12/26"
    }

    @Test
    @Description("single-digit month and year are zero-padded")
    fun `pads month and year`() {
        ExpiryDate(expYear = 2031, expMonth = 3).toDisplayString() shouldBe "03/31"
    }
}
