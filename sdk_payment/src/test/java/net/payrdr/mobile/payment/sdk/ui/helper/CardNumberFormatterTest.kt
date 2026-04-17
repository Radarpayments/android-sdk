package net.payrdr.mobile.payment.sdk.ui.helper

import android.content.Context
import android.content.res.Resources
import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.payrdr.mobile.payment.sdk.R
import org.junit.Test

@SmallTest
class CardNumberFormatterTest {

    private val context = mockk<Context>()
    private val resources = mockk<Resources>()

    private fun stubPanPattern() {
        every { context.resources } returns resources
        every {
            resources.getString(R.string.payrdr_card_list_pan_pattern, any<String>())
        } answers {
            "** ${secondArg<String>()}"
        }
    }

    @Test
    fun shouldMaskPanToLastFourDigits() {
        stubPanPattern()
        CardNumberFormatter.maskCardNumber(context, "4111111111111111") shouldBe "** 1111"
    }

    @Test
    fun shouldStripNonDigitsBeforeTakingLastFour() {
        stubPanPattern()
        CardNumberFormatter.maskCardNumber(context, "4111 1111 1111 4444") shouldBe "** 4444"
        CardNumberFormatter.maskCardNumber(context, "5555-5555-5555-1234") shouldBe "** 1234"
    }

    @Test
    fun shouldFallbackToOriginalWhenShorterThanFourDigits() {
        // When digit count < 4 the formatter passes the raw (unstripped) pan through.
        stubPanPattern()
        CardNumberFormatter.maskCardNumber(context, "12") shouldBe "** 12"
        CardNumberFormatter.maskCardNumber(context, "1-2-3") shouldBe "** 1-2-3"
    }

    @Test
    fun shouldHandleExactlyFourDigits() {
        stubPanPattern()
        CardNumberFormatter.maskCardNumber(context, "1234") shouldBe "** 1234"
    }
}
