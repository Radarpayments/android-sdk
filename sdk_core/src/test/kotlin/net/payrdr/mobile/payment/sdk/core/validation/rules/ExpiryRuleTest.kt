package net.payrdr.mobile.payment.sdk.core.validation.rules

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class ExpiryRuleTest {

    private val rule = ExpiryRule(code = "CODE", message = "MSG")

    @Test
    @Description("accepts MM/YY within allowed range")
    fun `accepts valid expiry`() {
        rule.validateForError("12/30").isValid shouldBe true
        rule.validateForError("01/00").isValid shouldBe true
        rule.validateForError("12/99").isValid shouldBe true
    }

    @Test
    @Description("accepts digits-only input")
    fun `accepts digits without separator`() {
        rule.validateForError("1230").isValid shouldBe true
    }

    @Test
    @Description("month below 1 fails")
    fun `rejects month zero`() {
        val result = rule.validateForError("00/30")
        result.isValid shouldBe false
        result.errorCode shouldBe "CODE"
        result.errorMessage shouldBe "MSG"
    }

    @Test
    @Description("month above 12 fails")
    fun `rejects month greater than 12`() {
        rule.validateForError("13/30").isValid shouldBe false
    }

    @Test
    @Description("non-digit input falls through to negative value and fails")
    fun `rejects non-numeric input`() {
        rule.validateForError("ab/cd").isValid shouldBe false
    }

    @Test
    @Description("empty string fails")
    fun `rejects empty string`() {
        rule.validateForError("").isValid shouldBe false
    }
}
